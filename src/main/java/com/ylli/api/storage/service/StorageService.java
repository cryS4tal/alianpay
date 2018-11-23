package com.ylli.api.storage.service;

import com.ylli.api.base.exception.AwesomeException;
import com.ylli.api.storage.Config;
import com.ylli.api.storage.mapper.StorageMapper;
import com.ylli.api.storage.model.Storage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.UUID;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Created by RexQian on 2017/3/14.
 */
@Service
public class StorageService {
    @Autowired
    StorageMapper storageMapper;

    @Value("${storage-path}")
    public String storagePath;

    @PostConstruct
    public void init() throws IOException {
        if (!Files.exists(Paths.get(storagePath))) {
            Files.createDirectory(Paths.get(storagePath));
        }
    }

    public Storage store(long committerId, MultipartFile file) throws AwesomeException {
        String filePath = genFilePath();
        Path path = Paths.get(storagePath, filePath);
        while (Files.exists(path)) {
            path = Paths.get(storagePath, filePath);
        }

        try {
            if (file.isEmpty()) {
                throw new AwesomeException(Config.ERROR_EMPTY_FILE);
            }
            Path parentPath = path.getParent();
            if (parentPath != null && !Files.exists(parentPath)) {
                Files.createDirectories(parentPath);
            }
            Files.copy(file.getInputStream(), path);
        } catch (IOException | SecurityException ex) {
            throw new AwesomeException(Config.ERROR_SAVE_FILE.format(file.getOriginalFilename()));
        }

        Storage storage = new Storage();
        storage.path = filePath;
        storage.committerId = committerId;
        storage.contentType = file.getContentType();
        storage.name = file.getOriginalFilename();

        storageMapper.insertSelective(storage);
        return storage;
    }

    public Storage get(long id) {
        return storageMapper.selectByPrimaryKey(id);
    }

    public Resource loadAsResource(long id) throws AwesomeException {
        Storage storage = storageMapper.selectByPrimaryKey(id);
        if (storage == null) {
            throw new AwesomeException(Config.ERROR_FILE_NOT_FOUND);
        }
        try {
            Path path = Paths.get(storagePath, storage.path);
            Resource resource = new UrlResource(path.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new AwesomeException(Config.ERROR_FILE_NOT_READ.format(storage.path));
            }
        } catch (MalformedURLException ex) {
            throw new AwesomeException(Config.ERROR_FILE_NOT_READ.format(storage.path));
        }
    }

    private String genFilePath() {
        Calendar calendar = Calendar.getInstance();
        return Paths
                .get(String.valueOf(calendar.get(Calendar.YEAR)),
                        String.valueOf(calendar.get(Calendar.MONTH) + 1),
                        String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)),
                        UUID.randomUUID().toString())
                .toString();
    }
}
