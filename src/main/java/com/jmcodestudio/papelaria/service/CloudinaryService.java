package com.jmcodestudio.papelaria.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.jmcodestudio.papelaria.config.CloudinaryProperties;
import com.jmcodestudio.papelaria.exception.RegraDeNegocioException;
import com.jmcodestudio.papelaria.exception.ServicoExternoIndisponivelException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/** UC-14b/c: upload de imagem de produto (RN-03 — 1 a 5 imagens, JPG/PNG/WebP, máx 5MB). */
@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private static final Set<String> TIPOS_ACEITOS = Set.of("image/jpeg", "image/png", "image/webp");

    private final CloudinaryProperties propriedades;
    private Cloudinary cloudinary;

    @PostConstruct
    void configurar() {
        cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", propriedades.cloudName(),
                "api_key", propriedades.apiKey(),
                "api_secret", propriedades.apiSecret(),
                "secure", true
        ));
    }

    public String enviar(MultipartFile arquivo) {
        if (arquivo.isEmpty()) {
            throw new RegraDeNegocioException("Arquivo de imagem vazio.");
        }
        if (!TIPOS_ACEITOS.contains(arquivo.getContentType())) {
            throw new RegraDeNegocioException("Formato de imagem inválido. Use JPG, PNG ou WebP.");
        }

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> resultado = cloudinary.uploader().upload(
                    arquivo.getBytes(),
                    ObjectUtils.asMap("folder", "loja-papelaria/produtos")
            );
            return (String) resultado.get("secure_url");

        } catch (IOException e) {
            throw new ServicoExternoIndisponivelException(
                    "Não foi possível enviar a imagem. Tente novamente.", e);
        }
    }
}
