package com.maze.internet_cafe;

import com.maze.internet_cafe.service.AgentService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class InternetCafeApplication {

	public static void main(String[] args) {
		ApplicationContext context =SpringApplication.run(InternetCafeApplication.class, args);
		// Get AgentService bean and register agent
		AgentService agentService = context.getBean(AgentService.class);
		agentService.registerAgent();
	}

}
