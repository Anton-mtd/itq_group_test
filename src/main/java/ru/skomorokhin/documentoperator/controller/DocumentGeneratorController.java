package ru.skomorokhin.documentoperator.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.skomorokhin.documentoperator.generator.DocumentGenerator;

@RestController
@RequestMapping("/api/generator")
public class DocumentGeneratorController {

    private final DocumentGenerator generator;

    public DocumentGeneratorController(DocumentGenerator generator) {
        this.generator = generator;
    }

    @PostMapping("/run")
    public String runGenerator() {
        new Thread(generator::run).start();
        return "Document generation started!";
    }
}