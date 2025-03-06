package com.diligrp.upay.shared.service;

public interface ILifeCycle 
{
	void start() throws Exception;
	
	void stop() throws Exception;
	
	boolean isRunning();
	
	boolean isStarted();
	
	String getState();
}