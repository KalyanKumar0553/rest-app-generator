package com.src.main.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ConfigPropertySaveRequestDTO {
	@NotBlank(message = "Category must not be blank")
    @Size(max = 100, message = "Category can contain up to 100 characters")
    private String category;

    @NotBlank(message = "Label must not be blank")
    @Size(max = 200, message = "Label can contain up to 200 characters")
    private String label;

    @NotBlank(message = "Property key must not be blank")
    @Size(max = 300, message = "Property key can contain up to 300 characters")
    private String propertyKey;

    @NotEmpty(message = "Allowed values list must not be empty")
    @Valid
    private List<ConfigPropertyValueDTO> values;

}
