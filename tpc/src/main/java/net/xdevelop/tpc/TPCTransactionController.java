package net.xdevelop.tpc;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Serve in the provider to receive the phase II commit/rollback requests from sponsor.
 * 
 * @author John.Huang
 */
@RestController
@RequestMapping("/tpc")
public class TPCTransactionController {
	@PostMapping("/commit/{tid}")
	public void commit(@PathVariable long tid) {
		TPCExecutorManager.commit(tid);
	}
	
	@PostMapping("/rollback/{tid}")
	public void rollback(@PathVariable long tid) {
		TPCExecutorManager.rollback(tid);
	}
}
