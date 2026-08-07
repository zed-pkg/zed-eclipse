# zed-eclipse

Eclipse integration work for Zed Package Manager state, diagnostics, and confirmation-gated recommended actions.

The dedicated repository now contains:

- a Java 21 process adapter using argv execution, bounded timeout, schema validation, failure redaction, and unsafe-action rejection;
- a multi-root workspace model for the future **Zed Packages** view;
- Problems-marker projections with file-specific resources and `.zpkg.toml` fallback markers;
- confirmation-gated quick-fix command previews carrying exact executable, argv, and working directory;
- JUnit coverage and cross-platform Maven CI.

```sh
mvn test
```

Remaining native work is the PDE/OSGi bundle, resource listeners, view and quick-fix UI wiring, clean Eclipse application tests, and p2 feature/update-site packaging.
