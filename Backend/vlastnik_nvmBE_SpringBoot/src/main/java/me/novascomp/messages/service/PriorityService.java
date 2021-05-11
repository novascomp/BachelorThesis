package me.novascomp.messages.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import me.novascomp.messages.model.General;
import me.novascomp.messages.model.Message;
import me.novascomp.messages.model.Priority;
import me.novascomp.messages.repository.MessageRepository;
import me.novascomp.messages.repository.PriorityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PriorityService extends GeneralService<Priority, PriorityRepository> {

    private final MessageRepository messageRepository;

    @Autowired
    public PriorityService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public Page<Priority> findByCreatorKey(String creatorKey, Pageable pageable) {
        return repository.findByCreatorKey(creatorKey, pageable);
    }

    public Page<Priority> findDistinctByMessageListIn(List<Message> messageList, Pageable pageable) {
        return repository.findDistinctByMessageListIn(messageList, pageable);
    }

    public void addPriority(Priority priority) {
        String id = UUID.randomUUID().toString();
        priority.setPriorityId(id);
        General general = nvfUtils.getGeneral(id);
        priority.setGeneral(general);
        repository.save(priority);
    }

    public void deletePriority(Priority priority) {
        for (Message message : priority.getMessageList()) {
            if (message.getPriorityList().contains(priority)) {
                message.getPriorityList().remove(priority);
                messageRepository.save(message);
            }
        }
        delete(priority);
    }

    public Optional<ResponseEntity> verifyPriorityRequest(Priority priority) {
        List<Priority> priorities = repository.findByCreatorKey(priority.getCreatorKey());

        for (Priority priorityRecord : priorities) {
            if (priorityRecord.getText().equals(priority.getText())) {
                return Optional.ofNullable(new ResponseEntity<>("priority EXISTS", HttpStatus.CONFLICT));

            }
        }
        return Optional.ofNullable(null);
    }
}
