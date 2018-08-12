package net.xdevelop.template.bank;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AccountControllerTPCTests {
	protected static final int THREADS = Runtime.getRuntime().availableProcessors();
	protected static int userNumber = 50;
	protected static int SIZE = 500;
	protected static double serialTPS = 0;
	protected static double paralleTPS = 0;
	protected static Random random = new Random();
	protected static String transfreUrl = "/accounts/User%d/tpc/transfer";

	@Autowired
	protected WebTestClient webClient;
	
    @Test
    public void test1SetAmountByUserId() throws Exception {
    	for (int i = 0; i < userNumber; i++) {
	    	String url = String.format("/accounts/User%d/amount?bank={bank}&amount={amount}", i);
	    	webClient.put().uri(url, "ALPHA", "1000000").accept(MediaType.APPLICATION_JSON)
	    	.exchange()
	    	.expectStatus().isOk();
	    	
	    	webClient.put().uri(url, "BRAVO", "1000000").accept(MediaType.APPLICATION_JSON)
	    	.exchange()
	    	.expectStatus().isOk();
    	}
    }
    
    @Test
    public void test2GetAmountByUserId() throws Exception {
    	double expectedResult = 1000000;
    	for (int i = 0; i < userNumber; i++) {
    		String url = String.format("/accounts/User%d/amount?bank={bank}", i);
	    	webClient.get().uri(url, "ALPHA").accept(MediaType.APPLICATION_JSON)
	    	.exchange()
	    	.expectStatus().isEqualTo(HttpStatus.OK)
	    	.expectBody(Double.class).isEqualTo(expectedResult);
	    	
	    	webClient.get().uri(url, "BRAVO").accept(MediaType.APPLICATION_JSON)
	    	.exchange()
	    	.expectStatus().isEqualTo(HttpStatus.OK)
	    	.expectBody(Double.class).isEqualTo(expectedResult);
    	}
    }
    
    @Test
    public void test3GetTotalByUserId() throws Exception {
    	for (int i = 0; i < userNumber; i++) {
    		String url = String.format("/accounts/User%d/amount?bank={bank}", i);
	    	double expectedResult = 2000000;
	    	webClient.get().uri(url, "ALL").accept(MediaType.APPLICATION_JSON)
	    	.exchange()
	    	.expectStatus().isEqualTo(HttpStatus.OK)
	    	.expectBody(Double.class).isEqualTo(expectedResult);
    	}
    }
    
    @Test
    public void test4SerialTransferTPC() throws Exception {
    	MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("bankTransferFrom", "ALPHA");
        formData.add("bankTransferTo", "BRAVO");
        
        long start = System.currentTimeMillis();
        for (int i = 0; i < SIZE; i++) {
        	doTransfer(transfreUrl, formData);
        }
        double time = (System.currentTimeMillis() - start) / 1000.0;
    	serialTPS = SIZE / time;
    }
    
	@Test
	public void test5ParalleTransfer() throws Exception {
		AtomicInteger control = new AtomicInteger(-1);

		List<Thread> threadList = new ArrayList<Thread>(THREADS);

		for (int i = 0; i < THREADS; i++) {
			Thread thread = new Thread(() -> workerRun(control));
			threadList.add(thread);
		}

		long start = System.currentTimeMillis();
		for (Thread thread : threadList) {
			thread.start();
		}

		// Wait for worker done
		for (Thread thread : threadList) {
			thread.join();
		}

		double time = (System.currentTimeMillis() - start) / 1000.0;
		paralleTPS = SIZE / time;

		Assert.assertEquals(SIZE, control.get());
	}
    
    @Test
    public void test6GetTotalByUserId() throws Exception {
    	// wait for timeout
    	Thread.sleep(10000);
    	for (int i = 0; i < userNumber; i++) {
	    	String url = String.format("/accounts/User%d/amount?bank={bank}", i);
	    	double expectedResult = 1000000 * 2;
	    	webClient.get().uri(url, "ALL").accept(MediaType.APPLICATION_JSON)
	    	.exchange()
	    	.expectStatus().isEqualTo(HttpStatus.OK)
	    	.expectBody(Double.class).isEqualTo(expectedResult);
    	}
    }
    
    protected void workerRun(AtomicInteger control) {
    	MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("bankTransferFrom", "ALPHA");
        formData.add("bankTransferTo", "BRAVO");
    	for (;;) {
    		int myPosition = control.updateAndGet(old -> (old == SIZE ? SIZE : old + 1));
    		if (myPosition == SIZE) {
                return;
            }
    		
    		doTransfer(transfreUrl, formData);
    	}
    }
    
    protected void doTransfer(String url, MultiValueMap<String, String> formData) {
    	url = String.format(url, random.nextInt(userNumber));
    	formData.set("amount", (random.nextInt(100) + 1) + "");
    	webClient.post().uri(url).accept(MediaType.APPLICATION_JSON)
    	.body(BodyInserters.fromFormData(formData))
    	.exchange();
    }
    
	@Test
	public void contextLoads() {
	}

	@Before
    public void setUp() throws Exception {
		
    }
	
	@AfterClass
	public static void printResult() throws Exception {
		System.out.println(String.format("TPC transaction serial TPS: %f", serialTPS));
		System.out.println(String.format("TPC transaction paralle TPS: %f", paralleTPS));
	}
}
