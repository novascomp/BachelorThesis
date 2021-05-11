package me.novascomp.messages.service;

import java.util.Optional;
import java.util.UUID;
import me.novascomp.messages.model.General;
import me.novascomp.messages.model.Re;
import me.novascomp.messages.repository.ReRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReService extends GeneralService<Re, ReRepository> {

    private final MessageService messageService;

    @Autowired
    public ReService(MessageService messageService) {
        this.messageService = messageService;
    }

    public Page<Re> findByCreatorKey(String creatorKey, Pageable pageable) {
        return repository.findByCreatorKey(creatorKey, pageable);
    }

    public void addRe(Re re) {
        String id = UUID.randomUUID().toString();
        re.setReId(id);
        General general = nvfUtils.getGeneral(id);
        re.setGeneral(general);
        re.setMessageId(messageService.findById(re.getMessageId().getMessageId()).get());
        repository.save(re);
    }

    public Optional<ResponseEntity> verifyReRequest(Re re) {

        if (re.getMessageId() == null) {
            return Optional.ofNullable(new ResponseEntity<>("message ID IS NULL", HttpStatus.BAD_REQUEST));
        }

        if (re.getHeading() == null) {
            return Optional.ofNullable(new ResponseEntity<>("heading IS NULL", HttpStatus.BAD_REQUEST));
        }

        if (re.getBody() == null) {
            return Optional.ofNullable(new ResponseEntity<>("body IS NULL", HttpStatus.BAD_REQUEST));
        }

        if (!(messageService.existsById(re.getMessageId().getMessageId()))) {
            return Optional.ofNullable(new ResponseEntity<>("message ID DOES NOT EXIST", HttpStatus.BAD_REQUEST));
        }

        return Optional.ofNullable(null);
    }
}
