package org.colorcoding.ibas.bobas.repository;

import org.colorcoding.ibas.bobas.mapping.Value;

/**
 * 事务类型
 * 
 * @author Niuren.Zhu
 *
 */
public enum TransactionType {
	/**
	 * 添加
	 */
	@Value("A")
	ADD,
	/**
	 * 更新
	 */
	@Value("U")
	UPDATE,
	/**
	 * 删除
	 */
	@Value("D")
	DELETE,
	/**
	 * 添加前
	 */
	@Value("T")
	BEFORE_ADD,
	/**
	 * 更新前
	 */
	@Value("G")
	BEFORE_UPDATE,
	/**
	 * 删除前
	 */
	@Value("S")
	BEFORE_DELETE,
}
