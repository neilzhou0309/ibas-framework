package org.colorcoding.ibas.bobas.db.pgsql.test;

import org.colorcoding.ibas.bobas.bo.IBOUserFields;
import org.colorcoding.ibas.bobas.bo.IUserField;
import org.colorcoding.ibas.bobas.common.ConditionRelationship;
import org.colorcoding.ibas.bobas.common.Criteria;
import org.colorcoding.ibas.bobas.common.ICondition;
import org.colorcoding.ibas.bobas.common.ICriteria;
import org.colorcoding.ibas.bobas.common.IOperationResult;
import org.colorcoding.ibas.bobas.common.ISort;
import org.colorcoding.ibas.bobas.common.SortType;
import org.colorcoding.ibas.bobas.data.DateTime;
import org.colorcoding.ibas.bobas.data.emDocumentStatus;
import org.colorcoding.ibas.bobas.core.RepositoryException;
import org.colorcoding.ibas.bobas.repository.InvalidTokenException;
import org.colorcoding.ibas.bobas.test.bo.ISalesOrder;
import org.colorcoding.ibas.bobas.test.bo.ISalesOrderItem;
import org.colorcoding.ibas.bobas.test.bo.IUser;
import org.colorcoding.ibas.bobas.test.bo.SalesOrder;
import org.colorcoding.ibas.bobas.test.bo.User;
import org.colorcoding.ibas.bobas.test.repository.BORepositoryTest;

import junit.framework.TestCase;

/**
 * Unit test for simple Amp.
 */
public class TestBORepository extends TestCase {

	public boolean details_out = true;

	public void testConnectBORepository() throws InvalidTokenException {
		// System.out.println(System.getProperty("java.class.path"));
		// System.out.println(System.getProperty("user.dir"));
		BORepositoryTest boRepository = new BORepositoryTest();
		boRepository.setUserToken("");
		DateTime dateTime = boRepository.getServerTime();
		System.out.println(dateTime.toString());
	}

	public void testBORepositoryTest() throws InvalidTokenException {
		BORepositoryTest boRepository = new BORepositoryTest();
		boRepository.setUserToken("");
		ISalesOrder order = new SalesOrder();
		order.setCustomerCode("C00001");
		order.setCustomerName("宇宙无敌影业");
		ISalesOrderItem item = order.getSalesOrderItems().create();
		item.setItemCode("T800");
		item.setItemDescription("终结者机器人-T800");
		item.setQuantity(1);
		item.setPrice(999999.99);
		item = order.getSalesOrderItems().create();
		item.setItemCode("S001");
		item.setItemDescription("绝地武士-剑");
		item.setQuantity(2);
		item.setPrice(99.00);
		IOperationResult<ISalesOrder> operationResult = boRepository.saveSalesOrder(order);
		System.out.println(String.format("code:%s message:%s results:%s", operationResult.getResultCode(),
				operationResult.getMessage(), operationResult.getResultObjects().size()));
		assertEquals(operationResult.getResultCode(), 0);

		ICriteria criteria = order.getCriteria();// new Criteria();
		criteria.setResultCount(1);
		ISort sort = criteria.getSorts().create();
		sort.setAlias(SalesOrder.PROPERTY_DOCENTRY.getName());
		sort.setSortType(SortType.DESCENDING);

		operationResult = boRepository.fetchSalesOrder(criteria);
		System.out.println(String.format("code:%s message:%s results:%s", operationResult.getResultCode(),
				operationResult.getMessage(), operationResult.getResultObjects().size()));
		assertEquals(operationResult.getResultCode(), 0);

	}

	public void testFetchBO() throws RepositoryException, InvalidTokenException {
		BORepositoryTest boRepository = new BORepositoryTest();
		// boRepository.connectRepository("MSSQL", "localhost", "ibas_demo",
		// "sa", "1q2w3e");
		boRepository.setUserToken("");
		ICriteria criteria = new Criteria();
		criteria.setResultCount(100);
		// ("DocStatus" = 'P' OR "DocStatus" = 'F')
		ICondition condition = criteria.getConditions().create();
		condition.setBracketOpen(1);
		condition.setAlias(SalesOrder.PROPERTY_DOCUMENTSTATUS.getName());
		condition.setValue(emDocumentStatus.PLANNED);
		condition = criteria.getConditions().create();
		condition.setBracketClose(1);
		condition.setAlias(SalesOrder.PROPERTY_DOCUMENTSTATUS.getName());
		condition.setValue(emDocumentStatus.RELEASED);
		condition.setRelationship(ConditionRelationship.OR);
		// ORDER BY "DocEntry" DESC, "CardCode" ASC
		ISort sort = criteria.getSorts().create();
		sort.setAlias(SalesOrder.PROPERTY_DOCENTRY.getName());
		sort.setSortType(SortType.DESCENDING);
		sort = criteria.getSorts().create();
		sort.setAlias(SalesOrder.PROPERTY_CUSTOMERCODE.getName());
		sort.setSortType(SortType.ASCENDING);

		IOperationResult<?> operationResult = boRepository.fetchSalesOrder(criteria);
		System.out.println(String.format("code:%s message:%s results:%s", operationResult.getResultCode(),
				operationResult.getMessage(), operationResult.getResultObjects().size()));
		assertEquals(operationResult.getResultCode(), 0);
		for (int i = 0; i < 3; i++) {
			operationResult = boRepository.fetchSalesOrder(criteria);
			System.out.println(String.format("code:%s message:%s results:%s", operationResult.getResultCode(),
					operationResult.getMessage(), operationResult.getResultObjects().size()));
			assertEquals(operationResult.getResultCode(), 0);
		}
		if (details_out) {
			for (Object item : operationResult.getResultObjects()) {
				if (item instanceof IBOUserFields) {
					IBOUserFields userFields = (IBOUserFields) item;
					System.out.println(String.format("%s user fields count:%s", item.toString(),
							userFields.getUserFields().size()));
					for (IUserField field : userFields.getUserFields()) {
						System.out.println(
								String.format(" %s %s %s", field.getName(), field.getValue(), field.getValueType()));
					}
				}
				System.out.println(String.format("%s complex field value:%s", item.toString(),
						((ISalesOrder) item).getCycle().toString()));
			}

		}
	}

	public void testSaveBO() throws RepositoryException, InvalidTokenException {
		BORepositoryTest boRepository = new BORepositoryTest();
		// boRepository.connectRepository("MSSQL", "localhost", "ibas_demo",
		// "sa", "1q2w3e");
		boRepository.setUserToken("");
		ISalesOrder order = new SalesOrder();
		order.setDocumentUser(new User());
		order.getDocumentUser().setUserCode(DateTime.getNow().toString("HHmmss") + "00");
		order.setTeamUsers(new User[] { new User(), new User() });
		order.getTeamUsers()[0].setUserCode(DateTime.getNow().toString("HHmmss") + "01");
		order.getTeamUsers()[1].setUserCode(DateTime.getNow().toString("HHmmss") + "02");
		order.setCustomerCode("C00001");
		order.setCustomerName("宇宙无敌影业");
		ISalesOrderItem item = order.getSalesOrderItems().create();
		item.setItemCode("T800");
		item.setItemDescription("终结者机器人-T800");
		item.setQuantity(1);
		item.setPrice(999999.99);
		item = order.getSalesOrderItems().create();
		item.setItemCode("S001");
		item.setItemDescription("绝地武士-剑");
		item.setQuantity(2);
		item.setPrice(99.00);
		IOperationResult<?> operationResult = boRepository.saveSalesOrder(order);

		System.out.println(String.format("code:%s message:%s results:%s", operationResult.getResultCode(),
				operationResult.getMessage(), operationResult.getResultObjects().size()));
		assertEquals(operationResult.getResultCode(), 0);
		order.setCustomerName("宇宙无敌影业--");
		operationResult = boRepository.saveSalesOrder(order);
		System.out.println(String.format("code:%s message:%s results:%s", operationResult.getResultCode(),
				operationResult.getMessage(), operationResult.getResultObjects().size()));
		assertEquals(operationResult.getResultCode(), 0);
		order.delete();
		operationResult = boRepository.saveSalesOrder(order);
		System.out.println(String.format("code:%s message:%s results:%s", operationResult.getResultCode(),
				operationResult.getMessage(), operationResult.getResultObjects().size()));
		assertEquals(operationResult.getResultCode(), 0);

	}

	public void testBOAssociations() throws RepositoryException, InvalidTokenException {
		BORepositoryTest boRepository = new BORepositoryTest();
		// boRepository.connectRepository("MSSQL", "localhost", "ibas_demo",
		// "sa", "1q2w3e");
		boRepository.setUserToken("");
		ICriteria criteria = new Criteria();

		IOperationResult<?> operationResult = boRepository.fetchSalesOrder(criteria);
		System.out.println(String.format("code:%s message:%s results:%s", operationResult.getResultCode(),
				operationResult.getMessage(), operationResult.getResultObjects().size()));
		assertEquals(operationResult.getResultCode(), 0);
		ISalesOrder order = (ISalesOrder) operationResult.getResultObjects().firstOrDefault();
		System.out.println(order.toString("xml"));
		IUser documentUser = order.getDocumentUser();
		if (documentUser != null) {
			System.out.println(String.format("user:%s ", documentUser));
		}
		IUser[] teamUsers = order.getTeamUsers();
		if (teamUsers != null) {
			System.out.println(String.format("team user count:%s ", teamUsers.length));
		}
	}

	static boolean FLAG_STOP = false;

	public void testExtremeTask() {
		try {
			for (int i = 0; i < 8; i++) {
				Thread ts = new Thread() {
					@Override
					public void run() {
						TestBORepository test = new TestBORepository();
						test.details_out = false;
						while (!FLAG_STOP) {
							try {
								test.testSaveBO();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				};
				ts.start();
				Thread tf = new Thread() {
					@Override
					public void run() {
						TestBORepository test = new TestBORepository();
						test.details_out = false;
						while (!FLAG_STOP) {
							try {
								test.testFetchBO();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				};
				tf.start();
			}
			Thread.sleep(30000);
			FLAG_STOP = true;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
