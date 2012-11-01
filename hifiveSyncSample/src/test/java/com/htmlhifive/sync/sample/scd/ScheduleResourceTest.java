/**
 *
 */
package com.htmlhifive.sync.sample.scd;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mockit.Expectations;
import mockit.Mocked;

import org.junit.Test;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.htmlhifive.sync.exception.BadRequestException;
import com.htmlhifive.sync.exception.DuplicateIdException;
import com.htmlhifive.sync.resource.ResourceQuerySpecifications;
import com.htmlhifive.sync.sample.person.Person;
import com.htmlhifive.sync.sample.person.PersonRepository;

/**
 * <H3>
 * ScheduleResourceのテストクラス.</H3>
 *
 * @author kishigam
 */
@SuppressWarnings("serial")
public class ScheduleResourceTest {

    @Mocked
    private ScheduleRepository repository;

    @Mocked
    private ResourceQuerySpecifications<Schedule> querySpec;

    @Mocked
    private PersonRepository personRepository;

    @SuppressWarnings("unused")
    @Mocked
    private final SecurityContextHolder securityContextHolder = null;

    @Mocked
    private SecurityContext securityContext;

    @Mocked
    private Authentication authentication;

    /**
     * typeテストメソッド.
     */
    @Test
    public void testType() {
        assertThat(ScheduleResource.class, notNullValue());
    }

    /**
     * {@link ScheduleResource#ScheduleResource()}用テストメソッド.
     */
    @Test
    public void testInstantiation() {
        ScheduleResource target = new ScheduleResource();
        assertThat(target, notNullValue());
    }

    /**
     * {@link ScheduleResource#doGet(StringArray)}用テストメソッド.
     */
    @Test
    public void testDoGet() {

        // Arrange：正常系
        final ScheduleResource target = new ScheduleResource();

        final String scheduleId = "scheduleId";

        final Person person = new Person() {
            {
                setPersonId("personId");
                setName("personName");
            }
        };

        final Schedule expectedEntity = new Schedule(scheduleId) {
            {
                setUserBeans(new ArrayList<Person>() {
                    {
                        add(person);
                    }
                });
                setCreateUser(person);
            }
        };

        final ScheduleResourceItem expectedItem = new ScheduleResourceItem(scheduleId) {
            {
                setUserIds(new ArrayList<String>() {
                    {
                        add("personId");
                    }
                });
                setDates(new ArrayList<String>());
                setCreateUserName("personName");
            }
        };

        Map<String, ScheduleResourceItem> expectedMap =
                new HashMap<String, ScheduleResourceItem>() {
                    {
                        put(scheduleId, expectedItem);
                    }
                };

        new Expectations() {
            {
                setField(target, repository);
                setField(target, querySpec);
                setField(target, personRepository);

                repository.findOne(scheduleId);
                result = expectedEntity;
            }
        };

        // Act
        Map<String, ScheduleResourceItem> actual = target.doGet(scheduleId);

        // Assert：結果が正しいこと
        assertThat(actual, is(expectedMap));
    }

    /**
     * {@link ScheduleResource#doGet(StringArray)}用テストメソッド.<br>
     * リポジトリにデータが存在しない場合、{@link BadRequestException}がスローされる.
     */
    @Test(expected = BadRequestException.class)
    public void testDoGetFailBecauseOfNotExists() {

        // Arrange：異常系
        final ScheduleResource target = new ScheduleResource();

        final String scheduleId = "scheduleId";

        new Expectations() {
            {
                setField(target, repository);
                setField(target, querySpec);
                setField(target, personRepository);

                repository.findOne(scheduleId);
                result = null;
            }
        };

        // Act
        target.doGet(scheduleId);

        // 例外でなければ失敗
        fail();
    }

    /**
     * {@link ScheduleResource#doGetByQuery(Map, StringArray)}用テストメソッド.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testDoGetByQuery() {

        // Arrange：正常系
        final ScheduleResource target = new ScheduleResource();

        final String personId = "personId";

        final Person person = new Person() {
            {
                setPersonId(personId);
                setName("personName");
            }
        };

        final String scheduleId1 = "scheduleId1";
        final String scheduleId2 = "scheduleId2";

        final Schedule scheduleEntity1 = new Schedule(scheduleId1) {
            {
                setCreateUser(person);
            }
        };

        final ScheduleResourceItem scheduleItem1 = new ScheduleResourceItem(scheduleId1) {
            {
                setUserIds(new ArrayList<String>());
                setDates(new ArrayList<String>());
                setCreateUserName("personName");
            }
        };

        final String[] ids = new String[] { scheduleId1, scheduleId2 };

        final Map<String, String[]> conditions = new HashMap<String, String[]>() {
            {
                put("scheduleId", new String[] { scheduleId1 });
                put("personId", new String[] { personId });
            }
        };

        final List<Schedule> expectedScheduleList = new ArrayList<Schedule>() {
            {
                add(scheduleEntity1);
            }
        };

        final Map<String, ScheduleResourceItem> expectedScheduleMap =
                new HashMap<String, ScheduleResourceItem>() {
                    {
                        put(scheduleId1, scheduleItem1);
                    }
                };

        new Expectations() {
            {
                setField(target, repository);
                setField(target, querySpec);
                setField(target, personRepository);

                Specifications<Schedule> specs = (Specifications<Schedule>)any;

                querySpec.parseConditions(conditions, ids);
                result = specs;

                repository.findAll(specs);
                result = expectedScheduleList;
            }
        };

        // Act
        Map<String, ScheduleResourceItem> actual = target.doGetByQuery(conditions, ids);

        // Assert：結果が正しいこと
        assertThat(actual, is(expectedScheduleMap));
    }

    /**
     * {@link ScheduleResource#doGetByQuery(Map, String...)}用テストメソッド.
     * 識別子リストがnullの時は例外をそのままスロー.
     */
    @Test(expected = Exception.class)
    public void testDoGetByQueryFailBecauseOfNullIds() {

        // Arrange：異常系
        final ScheduleResource target = new ScheduleResource();

        final String[] ids = null;

        final Map<String, String[]> conditions = new HashMap<>();

        new Expectations() {
            {
                setField(target, repository);
                setField(target, querySpec);
                setField(target, personRepository);

                querySpec.parseConditions(conditions, ids);
                result = new NullPointerException();
            }
        };

        // Act
        target.doGetByQuery(conditions, ids);

        fail();
    }

    /**
     * {@link ScheduleResource#doGetByQuery(Map, String...)}用テストメソッド.
     * クエリ条件Mapがnullの時は条件なし(空のMap)と同様の結果となる.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testDoGetByQueryFailBecauseOfNullCondMap() {

        // Arrange：例外系
        final ScheduleResource target = new ScheduleResource();

        final String personId = "personId";

        final Person person = new Person() {
            {
                setPersonId(personId);
                setName("personName");
            }
        };

        final String scheduleId1 = "scheduleId1";
        final String scheduleId2 = "scheduleId2";

        final Schedule scheduleEntity1 = new Schedule(scheduleId1) {
            {
                setCreateUser(person);
            }
        };

        final Schedule scheduleEntity2 = new Schedule(scheduleId2) {
            {
                setCreateUser(person);
            }
        };

        final ScheduleResourceItem scheduleItem1 = new ScheduleResourceItem(scheduleId1) {
            {
                setUserIds(new ArrayList<String>());
                setDates(new ArrayList<String>());
                setCreateUserName("personName");
            }
        };

        final ScheduleResourceItem scheduleItem2 = new ScheduleResourceItem(scheduleId2) {
            {
                setUserIds(new ArrayList<String>());
                setDates(new ArrayList<String>());
                setCreateUserName("personName");
            }
        };

        final String[] ids = new String[] { scheduleId1, scheduleId2 };

        final Map<String, String[]> conditions = null;

        final List<Schedule> expectedScheduleList = new ArrayList<Schedule>() {
            {
                add(scheduleEntity1);
                add(scheduleEntity2);
            }
        };

        final Map<String, ScheduleResourceItem> expectedScheduleMap =
                new HashMap<String, ScheduleResourceItem>() {
                    {
                        put(scheduleId1, scheduleItem1);
                        put(scheduleId2, scheduleItem2);
                    }
                };

        new Expectations() {
            {
                setField(target, repository);
                setField(target, querySpec);
                setField(target, personRepository);

                Specifications<Schedule> specs = (Specifications<Schedule>)any;

                querySpec.parseConditions(conditions, ids);
                result = specs;

                repository.findAll(specs);
                result = expectedScheduleList;
            }
        };

        // Act
        Map<String, ScheduleResourceItem> actual = target.doGetByQuery(conditions, ids);

        // Assert：結果が正しいこと
        assertThat(actual, is(expectedScheduleMap));
    }

    /**
     * {@link ScheduleResource#doCreate(ScheduleResourceItem)}用テストメソッド.
     */
    @Test
    public void testDoCreate() throws Exception {

        // Arrange：正常系
        final ScheduleResource target = new ScheduleResource();

        final String scheduleId = "100";

        final Person person = new Person() {
            {
                setPersonId("personId");
            }
        };

        final ScheduleResourceItem newItem = new ScheduleResourceItem(scheduleId) {
            {
                setUserIds(new ArrayList<String>());
                setDates(new ArrayList<String>());
                setCreateUserName(null);
            }
        };

        final Schedule newEntity = new Schedule(scheduleId) {
            {
                setCreateUser(person);
            }
        };

        new Expectations() {
            {
                setField(target, repository);
                setField(target, querySpec);
                setField(target, personRepository);

                repository.exists(scheduleId);
                result = false;

                SecurityContextHolder.getContext();
                result = securityContext;

                securityContext.getAuthentication();
                result = authentication;

                authentication.getPrincipal();
                result = new Object() {
                    @Override
                    public String toString() {
                        return "personId";
                    }
                };

                personRepository.findOne("personId");
                result = person;

                repository.save(newEntity);
                result = newItem;
            }
        };

        // Act
        String actual = target.doCreate(newItem);

        // Assert：結果が正しいこと
        assertThat(actual, is(equalTo(scheduleId)));
    }

    /**
     * {@link ScheduleResource#doCreate(ScheduleResourceItem)}用テストメソッド.<br>
     * キー重複が発生した場合 {@link DuplicateIdException}がスローされる.
     */
    @Test(expected = DuplicateIdException.class)
    public void testDoCreateFailBecauseOfDuplicateSchedule() throws Exception {

        // Arrange：例外系
        final ScheduleResource target = new ScheduleResource();

        final String scheduleId = "100";

        final Person person = new Person() {
            {
                setPersonId("personId");
            }
        };

        final ScheduleResourceItem newItem = new ScheduleResourceItem(scheduleId) {
            {
                setUserIds(new ArrayList<String>());
                setDates(new ArrayList<String>());
                setCreateUserName(null);
            }
        };

        final ScheduleResourceItem duplicatedItem = new ScheduleResourceItem(scheduleId) {
            {
                setUserIds(new ArrayList<String>());
                setDates(new ArrayList<String>());
                setCreateUserName(null);
            }
        };

        final Schedule duplicatedEntity = new Schedule(scheduleId) {
            {
                setCreateUser(person);
            }
        };

        new Expectations() {
            {
                setField(target, repository);
                setField(target, querySpec);
                setField(target, personRepository);

                repository.exists(scheduleId);
                result = true;

                repository.findOne(scheduleId);
                result = duplicatedEntity;
            }
        };

        // Act
        try {
            target.doCreate(newItem);
            fail();
        } catch (DuplicateIdException e) {

            assertThat(e.getDuplicatedTargetItemId(), is(equalTo(scheduleId)));
            assertThat((ScheduleResourceItem)e.getCurrentItem(), is(equalTo(duplicatedItem)));

            throw e;
        }
    }

    /**
     * {@link ScheduleResource#doCreate(ScheduleResourceItem)}用テストメソッド.<br>
     * 参加者として関連するPersonが存在しない場合{@link BadRequestException}がスローされる.
     */
    @Test(expected = BadRequestException.class)
    public void testDoCreateFailBecauseOfRelatedPersonNotExists() throws Exception {

        // Arrange：異常系
        final ScheduleResource target = new ScheduleResource();

        final String scheduleId = "100";

        final ScheduleResourceItem newItem = new ScheduleResourceItem(scheduleId) {
            {
                setUserIds(new ArrayList<String>() {
                    {
                        add("personId");
                    }
                });
                setDates(new ArrayList<String>());
                setCreateUserName(null);
            }
        };

        new Expectations() {
            {
                setField(target, repository);
                setField(target, querySpec);
                setField(target, personRepository);

                repository.exists(scheduleId);
                result = false;

                personRepository.findOne("personId");
                result = null;
            }
        };

        // Act
        target.doCreate(newItem);

        // 例外でなければ失敗
        fail();
    }

    /**
     * {@link ScheduleResource#doCreate(ScheduleResourceItem)}用テストメソッド.<br>
     * セキュリティコンテキストから取得したユーザーがPersonとして存在しない場合、createUserはnullになり、
     * アイテムとして取得した時にcreateUserNameが空文字列になる.
     */
    @Test
    public void testDoCreateNullCreateUser() throws Exception {

        // Arrange：例外系
        final ScheduleResource target = new ScheduleResource();

        final String scheduleId = "100";

        final Person person = new Person() {
            {
                setPersonId("personId");
            }
        };

        final ScheduleResourceItem newItem = new ScheduleResourceItem(scheduleId) {
            {
                setUserIds(new ArrayList<String>() {
                    {
                        add("personId");
                    }
                });
                setDates(new ArrayList<String>());
                setCreateUserName(null);
            }
        };

        final Schedule newEntity = new Schedule(scheduleId) {
            {
                setUserBeans(new ArrayList<Person>() {
                    {
                        add(person);
                    }
                });

                setCreateUser(null);
            }
        };

        final Schedule foundEntity = new Schedule(scheduleId) {
            {
                setUserBeans(new ArrayList<Person>() {
                    {
                        add(person);
                    }
                });
                setCreateUser(null);
            }
        };

        final ScheduleResourceItem gotItem = new ScheduleResourceItem(scheduleId) {
            {
                setUserIds(new ArrayList<String>() {
                    {
                        add("personId");
                    }
                });
                setDates(new ArrayList<String>());
                setCreateUserName("");
            }
        };

        new Expectations() {
            {
                setField(target, repository);
                setField(target, querySpec);
                setField(target, personRepository);

                repository.exists(scheduleId);
                result = false;

                personRepository.findOne("personId");
                result = person;

                SecurityContextHolder.getContext();
                result = securityContext;

                securityContext.getAuthentication();
                result = authentication;

                authentication.getPrincipal();
                result = new Object() {
                    @Override
                    public String toString() {
                        return "createUser";
                    }
                };

                personRepository.findOne("createUser");
                result = null;

                repository.save(newEntity);
                result = newItem;

                repository.findOne(scheduleId);
                result = foundEntity;
            }
        };

        // Act
        String actualCreated = target.doCreate(newItem);
        // 取得
        ScheduleResourceItem actualGot = target.doGet(scheduleId).get(scheduleId);

        // Assert：結果が正しいこと
        assertThat(actualCreated, is(equalTo(scheduleId)));
        assertThat(actualGot, is(equalTo(gotItem)));
    }

    /**
     * {@link ScheduleResource#doCreate(ScheduleResourceItem)}用テストメソッド.<br>
     * nullが渡された場合、{@link NullPointerException}がスローされる.
     */
    @Test(expected = NullPointerException.class)
    public void testDoCreateFailBecauseOfNullInput() throws Exception {

        // Arrange：正常系
        final ScheduleResource target = new ScheduleResource();

        new Expectations() {
            {
                setField(target, repository);
                setField(target, querySpec);
                setField(target, personRepository);
            }
        };

        // Act
        target.doCreate(null);

        // 例外でなければ失敗
        fail();
    }

    /**
     * {@link ScheduleResource#doUpdate(ScheduleResourceItem)}用テストメソッド.
     */
    @Test
    public void testDoUpdate() {

        // Arrange：正常系
        final ScheduleResource target = new ScheduleResource();

        final String scheduleId = "100";

        final Person person1 = new Person() {
            {
                setPersonId("personId1");
            }
        };

        final Person person2 = new Person() {
            {
                setPersonId("personId2");
            }
        };

        final Person person3 = new Person() {
            {
                setPersonId("personId3");
            }
        };

        final ScheduleResourceItem updatingItem = new ScheduleResourceItem(scheduleId) {
            {
                setUserIds(new ArrayList<String>() {
                    {
                        add("personId2");
                        add("personId3");
                    }
                });
                setDates(new ArrayList<String>() {
                    {
                        add("2001/1/1");
                        add("2011/11/12");
                    }
                });
                setCreateUserName(null);
            }
        };

        final Schedule entityBeforeUpdate = new Schedule(scheduleId) {
            {
                setUserBeans(new ArrayList<Person>() {
                    {
                        add(person1);
                        add(person2);
                    }
                });
                setCreateUser(person1);
            }
        };
        entityBeforeUpdate.setDateBeans(new ArrayList<ScheduleDate>() {
            {
                add(new ScheduleDate(entityBeforeUpdate, 20010101));
                add(new ScheduleDate(entityBeforeUpdate, 20111111));
            }
        });

        final Schedule updatingEntity = new Schedule(scheduleId) {
            {
                setUserBeans(new ArrayList<Person>() {
                    {
                        add(person2);
                        add(person3);
                    }
                });
                setCreateUser(person1);
            }
        };
        updatingEntity.setDateBeans(new ArrayList<ScheduleDate>() {
            {
                add(new ScheduleDate(updatingEntity, 20010101));
                add(new ScheduleDate(updatingEntity, 20111112));
            }
        });

        new Expectations() {
            {
                setField(target, repository);
                setField(target, querySpec);
                setField(target, personRepository);

                repository.findOne(scheduleId);
                result = entityBeforeUpdate;

                personRepository.findOne("personId3");
                result = person3;

                repository.save(updatingEntity);
                result = updatingEntity;
            }
        };

        // Act
        String actual = target.doUpdate(updatingItem);

        // Assert：結果が正しいこと
        assertThat(actual, is(equalTo(scheduleId)));
    }

    /**
     * {@link ScheduleResource#doUpdate(ScheduleResourceItem)}用テストメソッド.<br>
     * 変更対象のScheduleが存在しない場合、{@link BadRequestException}がスローされる.
     */
    @Test(expected = BadRequestException.class)
    public void testDoUpdateFailBecauseOfScheduleNotExists() {

        // Arrange：正常系
        final ScheduleResource target = new ScheduleResource();

        final String scheduleId = "100";

        final ScheduleResourceItem updatingItem = new ScheduleResourceItem(scheduleId);

        new Expectations() {
            {
                setField(target, repository);
                setField(target, querySpec);
                setField(target, personRepository);

                repository.findOne(scheduleId);
                result = null;
            }
        };

        // Act
        target.doUpdate(updatingItem);

        // 例外じゃなければ失敗
        fail();
    }

    /**
     * {@link ScheduleResource#doUpdate(ScheduleResourceItem)}用テストメソッド.<br>
     * 更新アイテムデータに関連するPersonが存在しない場合、{@link BadRequestException}がスローされる.
     */
    @Test(expected = BadRequestException.class)
    public void testDoUpdateFailBecauseOfRelatedPersonNotExists() {

        // Arrange：異常系
        final ScheduleResource target = new ScheduleResource();

        final String scheduleId = "100";

        final Person person1 = new Person() {
            {
                setPersonId("personId1");
            }
        };

        final Person person2 = new Person() {
            {
                setPersonId("personId2");
            }
        };

        final ScheduleResourceItem updatingItem = new ScheduleResourceItem(scheduleId) {
            {
                setUserIds(new ArrayList<String>() {
                    {
                        add("notFound");
                    }
                });
                setDates(new ArrayList<String>() {
                    {
                        add("2001/1/1");
                    }
                });
                setCreateUserName(null);
            }
        };

        final Schedule entityBeforeUpdate = new Schedule(scheduleId) {
            {
                setUserBeans(new ArrayList<Person>() {
                    {
                        add(person1);
                        add(person2);
                    }
                });
                setCreateUser(person1);
            }
        };
        entityBeforeUpdate.setDateBeans(new ArrayList<ScheduleDate>() {
            {
                add(new ScheduleDate(entityBeforeUpdate, 20010101));
            }
        });

        new Expectations() {
            {
                setField(target, repository);
                setField(target, querySpec);
                setField(target, personRepository);

                repository.findOne(scheduleId);
                result = entityBeforeUpdate;

                personRepository.findOne("notFound");
                result = null;
            }
        };

        // Act
        target.doUpdate(updatingItem);

        // 例外でなければ失敗
        fail();
    }

    /**
     * {@link ScheduleResource#doUpdate(ScheduleResourceItem)}用テストメソッド.<br>
     * 変更対象のScheduleが存在しない場合、{@link NullPointerException}がスローされる.
     */
    @Test(expected = NullPointerException.class)
    public void testDoUpdateFailBecauseOfNullInput() {

        // Arrange：正常系
        final ScheduleResource target = new ScheduleResource();

        new Expectations() {
            {
                setField(target, repository);
                setField(target, querySpec);
                setField(target, personRepository);
            }
        };

        // Act
        target.doUpdate(null);

        // 例外じゃなければ失敗
        fail();
    }

    /**
     * {@link ScheduleResource#doDelete(String)}用テストメソッド.
     */
    @Test
    public void testDoDelete() {

        // Arrange：正常系
        final ScheduleResource target = new ScheduleResource();

        final String scheduleId = null;

        final Schedule existing = new Schedule();
        existing.setScheduleId(scheduleId);
        existing.setTitle("toDelete");

        new Expectations() {
            {
                setField(target, repository);
                setField(target, querySpec);
                setField(target, personRepository);

                repository.findOne(scheduleId);
                result = existing;

                // 論理削除のため、repository#deleteは呼ばれない
            }
        };

        // Act
        ScheduleResourceItem actual = target.doDelete(scheduleId);

        // Assert：結果が正しいこと
        // 論理削除のため、personIdのみセットされたアイテムが返される.
        ScheduleResourceItem expected = new ScheduleResourceItem(scheduleId);
        assertThat(actual, is(equalTo(expected)));
    }

    /**
     * {@link ScheduleResource#doDelete(String)}用テストメソッド.<br>
     * 更新するScheduleが存在しない場合{@link BadRequestException}がスローされる.
     */
    @Test(expected = BadRequestException.class)
    public void testDoDeleteFailBecauseOfNotFound() {

        // Arrange：異常系
        final ScheduleResource target = new ScheduleResource();

        final String scheduleId = null;

        new Expectations() {
            {
                setField(target, repository);
                setField(target, querySpec);
                setField(target, personRepository);

                repository.findOne(scheduleId);
                result = null;
            }
        };

        // Act
        target.doDelete(scheduleId);

        // 例外でなければ失敗
        fail();
    }

    /**
     * {@link ScheduleResource#doDelete(String)}用テストメソッド.<br>
     * nullが渡された場合 {@link BadRequestException}がスローされる.
     */
    @Test(expected = BadRequestException.class)
    public void testDoDeleteFailBecauseOfNullInput() {

        // Arrange：異常系
        final ScheduleResource target = new ScheduleResource();

        new Expectations() {
            {
                setField(target, repository);
                setField(target, querySpec);
                setField(target, personRepository);
            }
        };

        // Act
        target.doDelete(null);

        // 例外でなければ失敗
        fail();
    }
}
