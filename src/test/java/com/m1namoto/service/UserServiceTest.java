package com.m1namoto.service;

import com.google.common.base.Optional;
import com.m1namoto.dao.HibernateUtil;
import com.m1namoto.domain.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class UserServiceTest {

    private static final UserService USER_SERVICE = UserService.getInstance();
    private static Transaction transaction;

    @Before
    public void setUp() {
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        Session currentSession = sessionFactory.getCurrentSession();
        transaction = currentSession.beginTransaction();
    }

    @After
    public void cleanUp() {
        if (transaction != null) {
            transaction.rollback();
        }
        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        Session currentSession = sessionFactory.getCurrentSession();
        currentSession.close();
    }

    @Test
    public void getList() throws Exception {
        prepareUsers();
        List<User> list = USER_SERVICE.getList();
        assertEquals(3, list.size());
    }

    @Test
    public void findByLogin_yes() throws Exception {
        prepareUsers();
        Optional<User> found = USER_SERVICE.findByLogin("ivan");
        assertTrue(found.isPresent());
        assertEquals("ivan", found.get().getLogin());
    }

    @Test
    public void findByLogin_no() throws Exception {
        prepareUsers();
        Optional<User> found = USER_SERVICE.findByLogin("ivann");
        assertFalse(found.isPresent());
    }

    @Test
    public void findById_yes() throws Exception {
        prepareUsers();
        Optional<User> foundByLogin = USER_SERVICE.findByLogin("ivan");
        Optional<User> foundById = USER_SERVICE.findById(foundByLogin.get().getId());
        assertTrue(foundById.isPresent());
        assertEquals(foundByLogin, foundById);
    }

    @Test
    public void findById_no() throws Exception {
        prepareUsers();
        Optional<User> foundByLogin = USER_SERVICE.findByLogin("ivan");
        Optional<User> foundById = USER_SERVICE.findById(999);
        assertFalse(foundById.isPresent());
    }

    @Test
    public void save() throws Exception {
        prepareUsers();
        List<User> list = USER_SERVICE.getList();
        assertEquals(3, list.size());
    }

    @Test
    public void remove() throws Exception {
        prepareUsers();
        Optional<User> found = USER_SERVICE.findByLogin("ivan");
        USER_SERVICE.remove(found.get());
        found = USER_SERVICE.findByLogin("ivan");
        assertFalse(found.isPresent());
    }

    @Test
    public void removeAll() throws Exception {
        prepareUsers();
        List<User> list = USER_SERVICE.getList();
        assertFalse(list.isEmpty());
        USER_SERVICE.removeAll();
        list = USER_SERVICE.getList();
        assertTrue(list.isEmpty());
    }

    private void prepareUsers() {
        User user = new User();
        user.setName("Ivan Ivanov");
        user.setLogin("ivan");
        user.setPassword("password");
        user.setUserType(User.Type.REGULAR);

        USER_SERVICE.save(user);

        user = new User();
        user.setName("Petr Petrov");
        user.setLogin("petr");
        user.setPassword("password");
        user.setUserType(User.Type.REGULAR);

        USER_SERVICE.save(user);

        user = new User();
        user.setName("Nikolay Nikolayev");
        user.setLogin("nikolayev");
        user.setPassword("password");
        user.setUserType(User.Type.REGULAR);

        USER_SERVICE.save(user);
    }
}