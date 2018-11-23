package com.ylli.api.storage;

import com.ylli.api.base.annotation.Auth;
import com.ylli.api.base.auth.AuthSession;
import com.ylli.api.base.exception.AwesomeException;
import com.ylli.api.storage.model.Storage;
import com.ylli.api.storage.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * Created by RexQian on 2017/3/14.
 */
@RestController
@RequestMapping("/files")
@Auth
public class FileController {
    @Autowired
    StorageService storageService;

    @Autowired
    AuthSession authSession;

    @GetMapping("/{id}")
    @ResponseBody
    Object getFile(@PathVariable long id) throws AwesomeException {
        Resource file = storageService.loadAsResource(id);
        Storage storage = storageService.get(id);
        return ResponseEntity
                .ok()
                .contentType(MediaType.valueOf(storage.contentType))
                .body(file);
    }

    @PostMapping
    Object addFile(@RequestParam(value = "upload_file") MultipartFile file) throws AwesomeException {
        return storageService.store(authSession.getAuthId(), file);
    }
}
