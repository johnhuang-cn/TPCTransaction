package net.xdevelop.template.bank;

import static net.xdevelop.template.bank.AccountControllerTPCTests.SIZE;
import static net.xdevelop.template.bank.AccountControllerTPCTests.paralleTPS;
import static net.xdevelop.template.bank.AccountControllerTPCTests.random;
import static net.xdevelop.template.bank.AccountControllerTPCTests.serialTPS;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AccountControllerTests extends AccountControllerTPCTests {
    
    @Test
    public void test4SerialTransferTPC() throws Exception {
    	String url = "/accounts/User%d/transfer";
    	MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("bankTransferFrom", "ALPHA");
        formData.add("bankTransferTo", "BRAVO");
        
        long start = System.currentTimeMillis();
        for (int i = 0; i < SIZE; i++) {
        	doTransfer(url, formData);
        }
        double time = (System.currentTimeMillis() - start) / 1000.0;
    	serialTPS = SIZE / time;
    }
    
    protected void workerRun(AtomicInteger control) {
    	MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("bankTransferFrom", "ALPHA");
        formData.add("bankTransferTo", "BRAVO");
        String url = "/accounts/User%d/transfer";
    	for (;;) {
    		int myPosition = control.updateAndGet(old -> (old == SIZE ? SIZE : old + 1));
    		if (myPosition == SIZE) {
                return;
            }
    		
    		doTransfer(url, formData);
    	}
    }
    
    protected void doTransfer(String url, MultiValueMap<String, String> formData) {
    	url = String.format(url, random.nextInt(20));
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
		System.out.println(String.format("Non transaction serial TPS: %f", serialTPS));
		System.out.println(String.format("Non transaction paralle TPS: %f", paralleTPS));
	}
}
