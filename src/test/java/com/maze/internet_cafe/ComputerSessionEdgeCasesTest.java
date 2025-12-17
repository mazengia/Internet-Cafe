//package com.maze.internet_cafe;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.maze.internet_cafe.branch.Branch;
//import com.maze.internet_cafe.branch.BranchRepository;
//import com.maze.internet_cafe.computer.dto.ComputerCreateDto;
//import com.maze.internet_cafe.session.dto.SessionStartRequest;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
//import org.springframework.http.MediaType;
//import org.springframework.security.test.context.support.WithMockUser;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.math.BigDecimal;
//
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@SpringBootTest
//@AutoConfigureMockMvc(addFilters = false)
//@ActiveProfiles("test")
//public class ComputerSessionEdgeCasesTest {
//
//    @Autowired
//    MockMvc mockMvc;
//
//    ObjectMapper mapper;
//
//    @Autowired
//    BranchRepository branchRepository;
//
//    @BeforeEach
//    void init() {
//        mapper = new ObjectMapper();
//    }
//
//    @Test
//    @WithMockUser(roles = {"ADMIN"})
//    void duplicateMacReturnsConflict() throws Exception {
//        Branch b = new Branch();
//        b.setName("Main Branch");
//        b.setCity("TestCity");
//        b.setPricePerHour(BigDecimal.valueOf(5.00));
//        Branch savedBranch = branchRepository.save(b);
//
//        ComputerCreateDto c1 = new ComputerCreateDto();
//        c1.setName("PC-1");
//        c1.setMacAddress("AA:BB:CC:DD:EE:11");
//        c1.setIpAddress("10.0.0.11");
//        c1.setBranchId(savedBranch.getId());
//
//        mockMvc.perform(post("/api/v1/computers")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(mapper.writeValueAsString(c1)))
//                .andExpect(status().isCreated());
//
//        ComputerCreateDto c2 = new ComputerCreateDto();
//        c2.setName("PC-2");
//        c2.setMacAddress("AA:BB:CC:DD:EE:11");
//        c2.setIpAddress("10.0.0.12");
//        c2.setBranchId(savedBranch.getId());
//
//        mockMvc.perform(post("/api/v1/computers")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(mapper.writeValueAsString(c2)))
//                .andExpect(status().isConflict());
//    }
//
//    @Test
//    @WithMockUser(roles = {"ADMIN"})
//    void startOnInUseComputerReturnsConflict() throws Exception {
//        Branch b = new Branch();
//        b.setName("Main Branch 2");
//        b.setCity("TestCity");
//        b.setPricePerHour(BigDecimal.valueOf(5.00));
//        Branch savedBranch = branchRepository.save(b);
//
//        ComputerCreateDto c = new ComputerCreateDto();
//        c.setName("PC-PLAY");
//        c.setMacAddress("AA:BB:CC:DD:EE:22");
//        c.setIpAddress("10.0.0.22");
//        c.setBranchId(savedBranch.getId());
//
//        String created = mockMvc.perform(post("/api/v1/computers")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(mapper.writeValueAsString(c)))
//                .andExpect(status().isCreated())
//                .andReturn().getResponse().getContentAsString();
//
//        JsonNode node = mapper.readTree(created);
//        long computerId = node.get("id").asLong();
//
//        SessionStartRequest req = new SessionStartRequest();
//        req.setPricePerHour(BigDecimal.valueOf(5.00));
//
//        String started = mockMvc.perform(post("/api/v1/computers/" + computerId + "/sessions/start")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(mapper.writeValueAsString(req)))
//                .andExpect(status().isCreated())
//                .andReturn().getResponse().getContentAsString();
//
//        // Attempt to start a second session on the same computer -> expect conflict
//        mockMvc.perform(post("/api/v1/computers/" + computerId + "/sessions/start")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(mapper.writeValueAsString(req)))
//                .andExpect(status().is4xxClientError());
//    }
//
//    @Test
//    @WithMockUser(roles = {"ADMIN"})
//    void stopNotRunningSessionReturnsConflict() throws Exception {
//        Branch b = new Branch();
//        b.setName("Main Branch 3");
//        b.setCity("TestCity");
//        b.setPricePerHour(BigDecimal.valueOf(5.00));
//        Branch savedBranch = branchRepository.save(b);
//
//        ComputerCreateDto c = new ComputerCreateDto();
//        c.setName("PC-STOP");
//        c.setMacAddress("AA:BB:CC:DD:EE:33");
//        c.setIpAddress("10.0.0.33");
//        c.setBranchId(savedBranch.getId());
//
//        String created = mockMvc.perform(post("/api/v1/computers")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(mapper.writeValueAsString(c)))
//                .andExpect(status().isCreated())
//                .andReturn().getResponse().getContentAsString();
//
//        JsonNode node = mapper.readTree(created);
//        long computerId = node.get("id").asLong();
//
//        // Try to stop a non-existent session id 9999 -> expect 4xx
//        mockMvc.perform(post("/api/v1/computers/" + computerId + "/sessions/9999/stop"))
//                .andExpect(status().is4xxClientError());
//    }
//}
