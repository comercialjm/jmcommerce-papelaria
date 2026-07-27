package com.jmcodestudio.papelaria.controller.api.admin;

import com.jmcodestudio.papelaria.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/** UC-14b/c: recebe o arquivo de imagem do formulário do admin e envia ao Cloudinary. */
@RestController
@RequestMapping("/admin/api/upload")
@RequiredArgsConstructor
public class UploadAdminApiController {

    private final CloudinaryService cloudinaryService;

    @PostMapping("/imagem")
    public Map<String, String> enviarImagem(@RequestParam("arquivo") MultipartFile arquivo) {
        String url = cloudinaryService.enviar(arquivo);
        return Map.of("url", url);
    }

}
