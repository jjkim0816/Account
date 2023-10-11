package com.zerobase.account.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zerobase.account.service.RedisTestService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class RedisTestController {
	private final RedisTestService redisTestService;

	@GetMapping("/get-lock")
	public String getLock() {
		return redisTestService.getLock();
	}
}
