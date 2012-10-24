/**
 *
 */
package com.htmlhifive.sync.sample.person;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import javax.annotation.Resource;
import javax.persistence.Table;

import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.transaction.TransactionConfiguration;

/**
 * <H3>
 * PersonRepositoryのテストクラス.</H3>
 *
 * @author kishigam
 */
@ContextConfiguration(locations = "classpath:test-context.xml")
@TransactionConfiguration(transactionManager = "txManager")
public class PersonRepositoryTest extends AbstractTransactionalJUnit4SpringContextTests {

    @SuppressWarnings("unused")
    private static final String TABLE_NAME = Person.class.getAnnotation(Table.class).name();

    @SuppressWarnings("unused")
    @Resource
    private PersonRepository target;

    @Test
    public void testTest() {

        assertThat(true, is(true));
    }
}
