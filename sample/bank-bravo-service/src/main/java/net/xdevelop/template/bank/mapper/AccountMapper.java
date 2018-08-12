package net.xdevelop.template.bank.mapper;

import org.apache.ibatis.annotations.Param;

public interface AccountMapper {
	/**
	 * Get amount of user id
	 * @param userId
	 * @return amount
	 */
	double getAmountByUserId(String userId);
	
	/**
	 * Set amount for user id
	 * @param userId
	 * @param amount
	 */
	void setAmountForUserId(@Param("userId") String userId, @Param("amount") double amount);
	
	/**
	 * Increase amount of user id
	 * @param userId
	 * @param amount
	 */
	int increaseAmount(@Param("userId") String userId, @Param("amount") double amount);
	
	/**
	 * Decrease amount of user id
	 * @param userId
	 * @param amount
	 */
	int decreaseAmount(@Param("userId") String userId, @Param("amount") double amount);
}