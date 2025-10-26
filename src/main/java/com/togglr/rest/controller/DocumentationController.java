package com.togglr.rest.controller;

import jakarta.servlet.ServletContext;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequestMapping("/api/v1/docs")
@RequiredArgsConstructor
@Tag(name = "Documentation", description = "API documentation endpoints")
public class DocumentationController {

    private final ServletContext servletContext;

    @GetMapping
    @Operation(
        summary = "API Documentation", 
        description = "Redirects to Scalar API documentation interface"
    )
    @ApiResponse(responseCode = "302", description = "Redirect to Scalar documentation")
    public RedirectView redirectToScalar() {
        return new RedirectView("/api/v1/docs/scalar");
    }

    @GetMapping(value = "/scalar", produces = "text/html")
    @Operation(
        summary = "Get Scalar documentation", 
        description = "Returns HTML page with Scalar API documentation interface (modern, interactive)"
    )
    @ApiResponse(responseCode = "200", description = "Scalar documentation page returned successfully")
    public String getScalarDocumentation() {
        String contextPath = servletContext.getContextPath();
        String apiDocsUrl = contextPath + "/v3/api-docs";

        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "<title>Togglr API Documentation</title>\n" +
                "<meta charset=\"utf-8\" />\n" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\" />\n" +
                "</head>\n" +
                "<body>\n" +
                "<script id=\"api-reference\" data-url=\"" + apiDocsUrl + "\"></script>\n" +
                "<script src=\"https://cdn.jsdelivr.net/npm/@scalar/api-reference\"></script>\n" +
                "</body>\n" +
                "</html>";
    }
}