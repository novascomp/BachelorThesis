package me.novascomp.messages.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import me.novascomp.messages.model.Category;
import me.novascomp.messages.model.General;
import me.novascomp.messages.model.Message;
import me.novascomp.messages.repository.CategoryRepository;
import me.novascomp.messages.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class CategoryService extends GeneralService<Category, CategoryRepository> {

    private final MessageRepository messageRepository;

    @Autowired
    public CategoryService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public Category findByCreatorKeyAndText(String creatorKey, String text) {
        return repository.findByCreatorKeyAndText(creatorKey, text);
    }

    public Page<Category> findByCreatorKey(String creatorKey, Pageable pageable) {
        return repository.findByCreatorKey(creatorKey, pageable);
    }

    public Page<Category> findDistinctByMessageListIn(List<Message> messageList, Pageable pageable) {
        return repository.findDistinctByMessageListIn(messageList, pageable);
    }

    public void addCategory(Category category) {
        String id = UUID.randomUUID().toString();
        category.setCategoryId(id);
        General general = nvfUtils.getGeneral(id);
        category.setGeneral(general);
        repository.save(category);
    }

    public void updateCategory(Category category) {
        repository.save(category);
    }

    public Optional<ResponseEntity> verifyCategoryRequest(Category category) {
        List<Category> categories = repository.findByCreatorKey(category.getCreatorKey());

        for (Category categoryRecord : categories) {
            if (categoryRecord.getText().equals(category.getText())) {
                return Optional.ofNullable(new ResponseEntity<>("category EXISTS", HttpStatus.CONFLICT));

            }
        }
        return Optional.ofNullable(null);
    }

}
