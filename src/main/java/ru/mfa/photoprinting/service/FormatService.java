package ru.mfa.photoprinting.service;

import ru.mfa.photoprinting.model.Format;
import java.util.List;

public interface FormatService {
    List<Format> getAllFormats();
    Format getFormatById(Long id);
    Format createFormat(Format format);
    Format updateFormat(Long id, Format format);
    void deleteFormat(Long id);
}