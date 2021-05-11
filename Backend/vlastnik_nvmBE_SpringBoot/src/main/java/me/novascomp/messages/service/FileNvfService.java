package me.novascomp.messages.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.sun.istack.NotNull;
import java.io.IOException;
import java.net.ConnectException;
import java.net.URISyntaxException;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import me.novascomp.messages.model.FileNvf;
import me.novascomp.messages.model.General;
import me.novascomp.messages.model.Message;
import me.novascomp.messages.repository.FileNvfRepository;
import me.novascomp.messages.repository.MessageRepository;
import me.novascomp.messages.services.requirement.FileDirectRequirement;
import me.novascomp.messages.services.requirement.FileViaMicroserviceRequirement;
import me.novascomp.microservice.communication.MicroserviceConnectionException;
import me.novascomp.microservice.communication.RestResponsePage;
import me.novascomp.microservice.nvf.FileResponse;
import me.novascomp.microservice.nvf.FileViaMicroserviceResponse;
import me.novascomp.microservice.nvf.MicroserviceNvf;
import me.novascomp.microservice.nvf.model.LightweightFileShare;
import me.novascomp.utils.standalone.service.exceptions.BadRequestException;
import me.novascomp.utils.standalone.service.exceptions.InternalException;
import me.novascomp.utils.standalone.service.exceptions.NotFoundException;
import me.novascomp.utils.standalone.service.exceptions.ServiceException;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FileNvfService extends GeneralService<FileNvf, FileNvfRepository> {

    private final MicroserviceNvf microserviceNvf;
    private final MessageRepository messageRepository;

    @Autowired
    public FileNvfService(MicroserviceNvf microserviceNvf, MessageRepository messageRepository) {
        this.microserviceNvf = microserviceNvf;
        this.messageRepository = messageRepository;
    }

    public Page<FileNvf> findByCreatorKey(String creatorKey, Pageable pageable) {
        return repository.findByCreatorKey(creatorKey, pageable);
    }

    public Page<FileNvf> findDistinctByMessageListIn(List<Message> messageList, Pageable pageable) {
        return repository.findDistinctByMessageListIn(messageList, pageable);
    }

    public Page<?> getFileShares(@NotNull String fileId, Pageable pageable) throws ServiceException {
        fileExistenceSecurity(fileId);

        try {
            FileNvf fileNvf = findById(fileId).get();
            HttpResponse<String> httpResponse = microserviceNvf.getFileShares(fileNvf.getFileIdInNvf(), pageable);
            Page<LightweightFileShare> shares = objectMapper.readValue(httpResponse.body(), new TypeReference<RestResponsePage<LightweightFileShare>>() {
            });
            for (LightweightFileShare LightweightFileShare : shares) {
                LightweightFileShare.setMessagesId(new ArrayList());
                fileNvf.getMessageList().forEach((message) -> {
                    LightweightFileShare.getMessagesId().add(message.getMessageId());
                });
            }
            return shares;
        } catch (ConnectException exception) {
            throw new MicroserviceConnectionException(exception.toString());
        } catch (IOException | InterruptedException exception) {
            throw new MicroserviceConnectionException(exception.toString());
        }
    }

    public FileViaMicroserviceResponse addFileViaNvfMicroservice(MultipartFile file) throws ServiceException {
        String futureFileID = UUID.randomUUID().toString();
        FileViaMicroserviceRequirement fileViaMicroserviceRequirement = getRequirement(file);
        if (fileViaMicroserviceRequirement.isValid()) {
            try {
                HttpResponse<String> httpResponse = microserviceNvf.uploadFile(file);
                HttpStatus httpStatus = HttpStatus.valueOf(httpResponse.statusCode());
                if (httpStatus.is2xxSuccessful()) {
                    Optional<String> fileIdInNvf = httpResponse.headers().firstValue("fileId");
                    if (fileIdInNvf.isEmpty()) {
                        throw new MicroserviceConnectionException("");
                    }
                    LOG.log(Level.INFO, "File: {0} created - FILE IN NVF ID: {1}", new Object[]{file.getOriginalFilename(), fileIdInNvf.get()});
                    FileNvf createdFileRecord = addFile(futureFileID, fileIdInNvf.get());
                    return new FileViaMicroserviceResponse(Optional.ofNullable(createdFileRecord), fileViaMicroserviceRequirement, httpStatus);
                } else {
                    httpStatusCodeToException(httpStatus);
                    return null;
                }
            } catch (ConnectException exception) {
                throw new MicroserviceConnectionException(exception.toString());
            } catch (URISyntaxException | IOException | InterruptedException exception) {
                throw new MicroserviceConnectionException(exception.toString());
            }
        } else {
            throw new BadRequestException("");
        }
    }

    public FileResponse addFileDirect(String fileInNvfId) throws ServiceException {
        String futureFileID = UUID.randomUUID().toString();
        FileDirectRequirement fileRequirement = getRequirement(fileInNvfId);

        if (fileRequirement.isValid()) {
            try {
                HttpResponse<String> httpResponse = microserviceNvf.checkFileExistence(fileInNvfId);
                HttpStatus httpStatus = HttpStatus.valueOf(httpResponse.statusCode());

                if (httpStatus.is2xxSuccessful()) {
                    FileNvf createdFileRecord = addFile(futureFileID, fileRequirement);
                    return new FileResponse(Optional.ofNullable(createdFileRecord), fileRequirement, HttpStatus.CREATED);
                } else {
                    return new FileResponse(Optional.ofNullable(null), fileRequirement, HttpStatus.NOT_FOUND);
                }

            } catch (IOException | InterruptedException exception) {
                throw new MicroserviceConnectionException(exception.toString());
            }
        }

        return new FileResponse(Optional.ofNullable(null), fileRequirement, HttpStatus.BAD_REQUEST);
    }

    private void deleteFileFromNVFMicroservice(@NotNull String fileId) throws ServiceException {

        fileExistenceSecurity(fileId);

        try {
            HttpResponse<String> httpResponse = microserviceNvf.deleteFile(findById(fileId).get().getFileIdInNvf());
            HttpStatus httpStatus = HttpStatus.valueOf(httpResponse.statusCode());
            if (httpStatus.is2xxSuccessful()) {
                return;
            }
        } catch (ConnectException exception) {
            throw new MicroserviceConnectionException(exception.toString());
        } catch (IOException | InterruptedException exception) {
            throw new InternalException("");
        }
        throw new InternalException("");
    }

    private FileNvf addFile(String id, String fileIdInNvf) {
        FileNvf fileNvf = new FileNvf();
        fileNvf.setFileNvfId(id);
        fileNvf.setFileIdInNvf(fileIdInNvf);
        General general = nvfUtils.getGeneral(id);
        fileNvf.setGeneral(general);
        repository.save(fileNvf);
        return fileNvf;
    }

    private FileNvf addFile(String id, FileDirectRequirement fileRequirement) {
        FileNvf fileNvf = null;

        if (fileRequirement.isValid()) {
            fileNvf = new FileNvf();
            fileNvf.setFileNvfId(id);
            fileNvf.setFileIdInNvf(fileRequirement.getFileInNvfId().get());
            General general = nvfUtils.getGeneral(id);
            fileNvf.setGeneral(general);
            repository.save(fileNvf);
        }
        return fileNvf;
    }

    @Transactional()
    public void deleteFile(FileNvf fileNvf) throws ServiceException {
        for (Message message : fileNvf.getMessageList()) {
            if (message.getFileNvfList().contains(fileNvf)) {
                message.getFileNvfList().remove(fileNvf);
                messageRepository.save(message);
            }
        }

        deleteFileFromNVFMicroservice(fileNvf.getFileNvfId());
        delete(fileNvf);
    }

    private FileViaMicroserviceRequirement getRequirement(MultipartFile file) {
        return new FileViaMicroserviceRequirement(Optional.ofNullable(file));
    }

    private FileDirectRequirement getRequirement(String fileInNvfId) {
        final Optional<FileNvf> fileNvf = repository.findByFileIdInNvf(fileInNvfId);
        return new FileDirectRequirement(fileNvf, Optional.ofNullable(fileInNvfId));
    }

    private void fileExistenceSecurity(String fileId) throws ServiceException {
        if (existsById(fileId) == false) {
            throw new NotFoundException("");
        }
    }

}
