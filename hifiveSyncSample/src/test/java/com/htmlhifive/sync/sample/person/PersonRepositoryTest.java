/**
 *
 */
package com.htmlhifive.sync.sample.person;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.persistence.Table;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.transaction.TransactionConfiguration;

import com.htmlhifive.sync.resource.common.ResourceItemCommonData;
import com.htmlhifive.sync.resource.common.ResourceItemCommonDataId;

/**
 * <H3>
 * PersonRepositoryのテストクラス.</H3>
 *
 * @author kishigam
 */
@ContextConfiguration(locations = "classpath:test-context.xml")
@TransactionConfiguration(transactionManager = "txManager")
@SuppressWarnings("serial")
public class PersonRepositoryTest extends AbstractTransactionalJUnit4SpringContextTests {

    private static final String TABLE_NAME = Person.class.getAnnotation(Table.class).name();

    @Resource
    private PersonRepository target;

    @Resource
    private PersonQuerySpecifications querySpecs;

    private Person personA;
    private Person personB;
    private Person personC;

    private ResourceItemCommonData commonA;
    private ResourceItemCommonData commonB;
    private ResourceItemCommonData commonC;

    @Before
    public void setUp() {

        deleteFromTables(TABLE_NAME);

        personA = new Person();
        personB = new Person();
        personC = new Person();

        personA.setPersonId("A");
        personA.setName("nameA");
        personA.setOrganization("org1");

        personB.setPersonId("B");
        personB.setName("nameB");
        personB.setOrganization("org2");

        personC.setPersonId("C");
        personC.setName("nameC");
        personC.setOrganization(personA.getOrganization());

        target.save(personA);
        target.save(personB);
        target.save(personC);

        commonA =
                new ResourceItemCommonData(
                        new ResourceItemCommonDataId("person", "common1"), personA.getPersonId());
        commonB =
                new ResourceItemCommonData(
                        new ResourceItemCommonDataId("person", "common2"), personB.getPersonId());
        commonC =
                new ResourceItemCommonData(
                        new ResourceItemCommonDataId("person", "common3"), personC.getPersonId());
    }

    @Test
    public void testFindAllBySpecOfPersonId() {

        List<ResourceItemCommonData> commonDataCond = new ArrayList<ResourceItemCommonData>() {
            {
                add(commonA);
                add(commonB);
                add(commonC);
            }
        };

        Map<String, String[]> personCond = new HashMap<String, String[]>() {
            {
                put("personId", new String[] {
                        personA.getPersonId(), personB.getPersonId(), personC.getPersonId() });
                put("organization", new String[] { personA.getOrganization() });
            }
        };

        List<Person> actual =
                target.findAll(querySpecs.parseConditions(commonDataCond, personCond));

        assertThat(actual.size(), is(equalTo(2)));
        assertThat(actual.contains(personA), is(true));
        assertThat(actual.contains(personB), is(false));
        assertThat(actual.contains(personC), is(true));
    }

    /**
     * 共通データの条件に合致しない場合は結果に含まれない.
     */
    @Test
    public void testNotFoundBecaouseOfCommonDataCond() {

        List<ResourceItemCommonData> commonDataCond = new ArrayList<ResourceItemCommonData>() {
            {
                // empty
            }
        };

        Map<String, String[]> personCond = new HashMap<String, String[]>() {
            {
                put("personId", new String[] { personA.getPersonId() });
                put(
                        "organization",
                        new String[] { personA.getOrganization(), personB.getOrganization() });
            }
        };

        List<Person> actual =
                target.findAll(querySpecs.parseConditions(commonDataCond, personCond));

        assertThat(actual.isEmpty(), is(true));
    }

    @Test
    public void testFindAllBySpecOfCommonDataCondOnly() {

        List<ResourceItemCommonData> commonDataCond = new ArrayList<ResourceItemCommonData>() {
            {
                add(commonA);
                add(commonB);
                add(commonC);
            }
        };

        Map<String, String[]> personCond = new HashMap<String, String[]>() {
            {
                // empty
            }
        };

        List<Person> actual =
                target.findAll(querySpecs.parseConditions(commonDataCond, personCond));

        assertThat(actual.size(), is(equalTo(3)));
        assertThat(actual.contains(personA), is(true));
        assertThat(actual.contains(personB), is(true));
        assertThat(actual.contains(personC), is(true));
    }
}
