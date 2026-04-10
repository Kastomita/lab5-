package ru.mfa.photoprinting.service.impl;

import ru.mfa.photoprinting.model.Format;
import ru.mfa.photoprinting.repository.FormatRepository;
import ru.mfa.photoprinting.service.FormatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class FormatServiceImpl implements FormatService {

    @Autowired
    private FormatRepository formatRepository;

    @Override
    public List<Format> getAllFormats() {
        return formatRepository.findAll();
    }

    @Override
    public Format getFormatById(Long id) {
        return formatRepository.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public Format createFormat(Format format) {
        return formatRepository.save(format);
    }

    @Override
    @Transactional
    public Format updateFormat(Long id, Format formatDetails) {
        Format format = formatRepository.findById(id).orElse(null);
        if (format == null) {
            return null;
        }
        format.setName(formatDetails.getName());
        format.setDescription(formatDetails.getDescription());
        format.setPrice(formatDetails.getPrice());
        format.setIsAvailable(formatDetails.getIsAvailable());
        return formatRepository.save(format);
    }

    @Override
    @Transactional
    public void deleteFormat(Long id) {
        formatRepository.deleteById(id);
    }
}