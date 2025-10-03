package com.example.snippetshare.snippet;

public enum Language {
    JAVA("java"),
    JAVASCRIPT("javascript"),
    TYPESCRIPT("typescript"),
    PYTHON("python"),
    GO("go"),
    CSHARP("csharp"),
    CPP("cpp"),
    HTML("markup"),
    CSS("css"),
    SQL("sql"),
    JSON("json"),
    YAML("yaml"),
    SHELL("bash");

    private final String prismAlias;

    Language(String prismAlias) {
        this.prismAlias = prismAlias;
    }

    public String getPrismAlias() {
        return prismAlias;
    }
}


