package com.maze.internet_cafe.computer;

import com.maze.internet_cafe.branch.Branch;
import com.maze.internet_cafe.computer.dto.ComputerCreateDto;
import com.maze.internet_cafe.computer.dto.ComputerDto;
import com.maze.internet_cafe.exception.EntityNotFoundException;
import com.maze.internet_cafe.branch.BranchRepository;
import com.maze.internet_cafe.service.ComputerCommand;
import com.maze.internet_cafe.service.LockService;
import com.maze.internet_cafe.session.SessionService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityExistsException;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class ComputerService {

    private final ComputerRepository computerRepository;
    private final SessionService sessionService;
    private final BranchRepository branchRepository;
    private final ModelMapper mapper = new ModelMapper();
    private final SimpMessagingTemplate messagingTemplate;


    public ComputerDto create(ComputerCreateDto dto) {
        Computer computer = computerRepository.findByName(dto.getName())
                .orElseGet(() -> {
                    Computer newComp = new Computer();
                    newComp.setMacAddress(dto.getMacAddress());
                    newComp.setStatus(ComputerStatus.AVAILABLE);
                    return newComp;
                });

        computer.setName(dto.getName());
        computer.setIpAddress(dto.getIpAddress());
        computer.setLastHeartbeat(LocalDateTime.now());

        if (dto.getOsType() != null) {
            try {
                computer.setOsType(OSType.valueOf(dto.getOsType()));
            } catch (Exception ignored) {
            }
        }

        if (computer.getBranch() == null) {
            Branch b = branchRepository.findById(1L).orElseThrow();
            computer.setBranch(b);
        }

        return toDto(computerRepository.save(computer));
    }


    public Page<ComputerDto> list(Long branchId, Pageable pageable) {
//        Page<Computer> page = computerRepository.findByBranchId(branchId, pageable);
        Page<Computer> page = computerRepository.findAll(pageable);
        return page.map(this::toDto);
    }

    public void handleHeartbeat(String name) {
        if (name != null) {
            computerRepository.findByName(name).ifPresent(computer -> {
                System.out.println(" computer status: " + computer.getStatus());
                computer.setLastHeartbeat(java.time.LocalDateTime.now());
                if( computer.getStatus() != ComputerStatus.IN_USE){
                    computer.setStatus(ComputerStatus.AVAILABLE);
                    computerRepository.save(computer);
                    LockService.lock();
                }
                computerRepository.save(computer);
            });
        }
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

    public void lockComputer(Long computerId) {

        Computer computer = computerRepository.findById(computerId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                Computer.class, "id", computerId.toString()
                        )
                );

        // ⏸ Pause session if running (billing-safe)
        sessionService.stopRunningSession(computerId);

        // 🔒 Send LOCK command via STOMP
        messagingTemplate.convertAndSend(
                "/topic/computers/" + computerId,
                new ComputerCommand(computerId, "LOCK")
        );

        // 🗃 Update status
        computer.setStatus(ComputerStatus.LOCKED);
        computerRepository.save(computer);
    }
}
