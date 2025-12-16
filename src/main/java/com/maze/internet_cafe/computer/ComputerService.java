package com.maze.internet_cafe.computer;

import com.maze.internet_cafe.branch.Branch;
import com.maze.internet_cafe.computer.dto.ComputerCreateDto;
import com.maze.internet_cafe.computer.dto.ComputerDto;
import com.maze.internet_cafe.exception.EntityNotFoundException;
import com.maze.internet_cafe.branch.BranchRepository;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityExistsException;
import java.time.LocalDateTime;

@Service
@Transactional
public class ComputerService {

    private final ComputerRepository computerRepository;
    private final BranchRepository branchRepository;
    private final ModelMapper mapper = new ModelMapper();

    public ComputerService(ComputerRepository computerRepository, BranchRepository branchRepository) {
        this.computerRepository = computerRepository;
        this.branchRepository = branchRepository;
    }

    public ComputerDto create(ComputerCreateDto dto) {
        // check duplicate MAC
        computerRepository.findByMacAddress(dto.getMacAddress()).ifPresent(c -> {
            throw new EntityExistsException("Computer with macAddress already exists");
        });

        Computer c = new Computer();
        c.setName(dto.getName());
        if (dto.getOsType() != null) {
            try {
                c.setOsType(OSType.valueOf(dto.getOsType()));
            } catch (Exception ignored) {
            }
        }
        c.setMacAddress(dto.getMacAddress());
        c.setIpAddress(dto.getIpAddress());
        c.setStatus(ComputerStatus.AVAILABLE);
        if (dto.getBranchId() != null) {

//            Branch b = branchRepository.findById(dto.getBranchId())
            Branch b = branchRepository.findById(Long.valueOf(1))
                    .orElseThrow(() -> new EntityNotFoundException(Branch.class, "id", dto.getBranchId().toString()));
            c.setBranch(b);
        }
        c.setLastHeartbeat(LocalDateTime.now());
        Computer saved = computerRepository.save(c);
        return toDto(saved);
    }

    public Page<ComputerDto> list(Long branchId, Pageable pageable) {
        Page<Computer> page = computerRepository.findByBranchId(branchId, pageable);
        return page.map(this::toDto);
    }

    public ComputerDto toDto(Computer c) {
        ComputerDto dto = mapper.map(c, ComputerDto.class);
        if (c.getBranch() != null) dto.setBranchId(c.getBranch().getId());
        return dto;
    }

    public Computer findById(Long id) {
        return computerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Computer.class, "id", id.toString()));
    }
}
