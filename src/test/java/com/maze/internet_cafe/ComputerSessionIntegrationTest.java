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
//public class ComputerSessionIntegrationTest {
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
//    void createComputerStartAndStopSession() throws Exception {
//        // create branch because Branch.pricePerHour is not nullable
//        Branch b = new Branch();
//        b.setName("Main Branch");
//        b.setCity("TestCity");
//        b.setPricePerHour(BigDecimal.valueOf(5.00));
//        Branch savedBranch = branchRepository.save(b);
//
//        // create computer
//        ComputerCreateDto c = new ComputerCreateDto();
//        c.setName("PC-INT-1");
//        c.setMacAddress("AA:BB:CC:DD:EE:FF");
//        c.setIpAddress("10.0.0.1");
//        c.setBranchId(savedBranch.getId());
//
//        String created = mockMvc.perform(post("/api/v1/computers")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(mapper.writeValueAsString(c)))
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.id").exists())
//                .andReturn().getResponse().getContentAsString();
//
//        JsonNode node = mapper.readTree(created);
//        long computerId = node.get("id").asLong();
//
//        // start session
//        SessionStartRequest req = new SessionStartRequest();
//        req.setPricePerHour(BigDecimal.valueOf(5.00));
//
//        String started = mockMvc.perform(post("/api/v1/computers/" + computerId + "/sessions/start")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(mapper.writeValueAsString(req)))
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.status").value("RUNNING"))
//                .andReturn().getResponse().getContentAsString();
//
//        JsonNode startedNode = mapper.readTree(started);
//        long sessionId = startedNode.get("id").asLong();
//
//        // stop session
//        mockMvc.perform(post("/api/v1/computers/" + computerId + "/sessions/" + sessionId + "/stop"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.status").value("FINISHED"));
//    }
//}
