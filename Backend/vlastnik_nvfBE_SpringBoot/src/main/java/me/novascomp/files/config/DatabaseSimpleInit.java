package me.novascomp.files.config;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import me.novascomp.files.model.Folder;
import me.novascomp.files.model.Server;
import me.novascomp.files.model.User;
import me.novascomp.files.services.FolderService;
import me.novascomp.files.services.ServerService;
import me.novascomp.files.services.UserService;
import org.springframework.beans.factory.annotation.Qualifier;

@Configuration
@PropertySource(name = "owncloud", value = "owncloud.properties")
public class DatabaseSimpleInit {

    @Value("${owncloud.server.path}")
    private String owncloudServerPath;

    @Value("${owncloud.webdav.path}")
    private String owncloudWebdavPath;

    @Value("${owncloud.ocs.path}")
    private String owncloudOcsPath;

    @Value("${owncloud.username}")
    private String owncloudUsername;

    @Value("${owncloud.password}")
    private String owncloudPassword;

    public static final String SIMPLE_USER_UUID = "SimpleUserUUID";
    public static final String SIMPLE_FOLDER_DEV_NAME = "Bakalarka";
    public static final String SIMPLE_FOLDER_PROD_NAME = "Bakalarka_prod";

    private final ServerService serverService;
    private final FolderService folderService;
    private final UserService userService;

    private final boolean production;

    @Autowired
    public DatabaseSimpleInit(ServerService serverService, FolderService folderService, UserService userService, @Qualifier("production") boolean production) {
        this.serverService = serverService;
        this.folderService = folderService;
        this.userService = userService;
        this.production = production;
    }

    public synchronized void initDataForSimpleRest() {

        if (!serverService.existsByBaseUrl(owncloudServerPath)) {
            initSimpleServer();
        }

        if (!userService.existsByUid(SIMPLE_USER_UUID)) {
            initSimpleUser();
        }

        if (production) {
            if (!folderService.existsByName(SIMPLE_FOLDER_PROD_NAME)) {
                initFolder(SIMPLE_FOLDER_PROD_NAME);
            }
        } else {
            if (!folderService.existsByName(SIMPLE_FOLDER_DEV_NAME)) {
                initFolder(SIMPLE_FOLDER_DEV_NAME);
            }
        }

    }

    public Optional<Server> getSimpleServer() {
        return serverService.findByBaseUrl(owncloudServerPath);
    }

    public Optional<User> getSimpleUser() {
        return userService.findByUid(SIMPLE_USER_UUID);
    }

    public Optional<Folder> getSimpleFolder() {
        List<Optional<Folder>> list;
        if (production) {
            list = folderService.findByName(SIMPLE_FOLDER_PROD_NAME);
        } else {
            list = folderService.findByName(SIMPLE_FOLDER_DEV_NAME);
        }

        for (Optional<Folder> folder : list) {
            if (folder.isPresent()) {
                if (folder.get().getPreviousFolderId() == null && folder.get().getServerId().getBaseUrl().equals(owncloudServerPath)) {
                    return folder;
                }
            }
        }
        return Optional.ofNullable(null);
    }

    private void initSimpleServer() {
        Server server = new Server();
        server.setBaseUrl(owncloudServerPath);
        server.setWebdavPath(owncloudWebdavPath);
        server.setOcsPath(owncloudOcsPath);
        server.setUsername(owncloudUsername);
        server.setPassword(owncloudPassword);
        serverService.addServer(server);
    }

    private void initFolder(String folderName) {
        Folder folder = new Folder();
        folder.setName(folderName);
        folder.setServerId(getSimpleServer().get());
        folder.setUserId(getSimpleUser().get());
        folderService.addFolder(folder);
    }

    private void initSimpleUser() {
        User user = new User();
        user.setUid(SIMPLE_USER_UUID);
        userService.addUser(user);
    }
}
