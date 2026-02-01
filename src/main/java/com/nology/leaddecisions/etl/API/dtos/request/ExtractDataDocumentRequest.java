package com.nology.leaddecisions.etl.API.dtos.request;

import com.nology.leaddecisions.etl.API.validation.XlsxFilename;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExtractDataDocumentRequest {
    @XlsxFilename
    private MultipartFile file;

}
