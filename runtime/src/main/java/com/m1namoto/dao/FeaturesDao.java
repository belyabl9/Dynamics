package com.m1namoto.dao;

import com.m1namoto.domain.*;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FeaturesDao extends GenericDAO<Feature> {

    private static final String HOLD_FEATURES_BY_CODE_QUERY = "FROM HoldFeature ORDER BY code";
    private static final String RELEASE_PRESS_FEATURES_QUERY = "FROM ReleasePressFeature ORDER BY releaseCode, pressCode";
    private static final String PRESS_PRESS_FEATURES_QUERY = "FROM PressPressFeature ORDER BY firstPressCode, secondPressCode";
    private static final String FEATURES_BY_USER_QUERY = "FROM Feature WHERE user_id = :user_id";
    private static final String HOLD_FEATURES_BY_USER_QUERY = "FROM HoldFeature WHERE user_id = :user_id";
    private static final String RELEASE_PRESS_FEATURES_BY_USER_QUERY = "FROM ReleasePressFeature WHERE user_id = :user_id";
    private static final String PRESS_PRESS_FEATURES_BY_USER_QUERY = "FROM PressPressFeature WHERE user_id = :user_id";
    private static final String SESSION_FEATURES_QUERY = "FROM Feature WHERE session_id = :session_id";

    public FeaturesDao(@NotNull SessionFactory factory) {
        super(Feature.class, factory);
    }

    /**
     * Returns a list of hold features
     */
    public List<HoldFeature> getHoldFeatures() {
        Session session = getFactory().getCurrentSession();
        Query query = session.createQuery(HOLD_FEATURES_BY_CODE_QUERY);
        return query.list();
    }

    /**
     * Returns a list of release-press features
     */
    public List<ReleasePressFeature> getReleasePressFeatures() {
        Session session = getFactory().getCurrentSession();
        Query query = session.createQuery(RELEASE_PRESS_FEATURES_QUERY);
        return query.list();
    }

    /**
     * Returns a list of press-press features
     */
    public List<PressPressFeature> getPressPressFeatures() {
        Session session = getFactory().getCurrentSession();
        Query query = session.createQuery(PRESS_PRESS_FEATURES_QUERY);
        return query.list();
    }

    /**
     * Returns a list of user features
     */
    public List<Feature> getList(@NotNull User user) {
        Session session = getFactory().getCurrentSession();
        Query query = session.createQuery(FEATURES_BY_USER_QUERY);
        query.setParameter("user_id", user.getId());
        return query.list();
    }

    /**
     * Returns a list of user hold features
     */
    public List<HoldFeature> getHoldFeatures(@NotNull User user) {
        Session session = getFactory().getCurrentSession();
        Query query = session.createQuery(HOLD_FEATURES_BY_USER_QUERY);
        query.setParameter("user_id", user.getId());
        return query.list();
    }
    
    /**
     * Returns a list of user release-press features
     */
    public List<ReleasePressFeature> getReleasePressFeatures(@NotNull User user) {
        Session session = getFactory().getCurrentSession();
        Query query = session.createQuery(RELEASE_PRESS_FEATURES_BY_USER_QUERY);
        query.setParameter("user_id", user.getId());
        return query.list();
    }

    /**
     * Returns a list of user release-press features
     */
    public List<PressPressFeature> getPressPressFeatures(@NotNull User user) {
        Session session = getFactory().getCurrentSession();
        Query query = session.createQuery(PRESS_PRESS_FEATURES_BY_USER_QUERY);
        query.setParameter("user_id", user.getId());
        return query.list();
    }
    
    /**
     * Returns a list of session features
     */
    public List<Feature> getSessionFeatures(@NotNull com.m1namoto.domain.Session session) {
        Session hibSession = getFactory().getCurrentSession();
        Query query = hibSession.createQuery(SESSION_FEATURES_QUERY);
        query.setParameter("session_id", session.getId());
        return query.list();
    }
    
    /**
     * Removes user features
     */
    public void removeAll(@NotNull User user) {
        List<Feature> features = getList(user);
        for (Feature feature : features) {
            delete(feature);
        }
    }
    
    /**
     * Removes all features
     */
    public void removeAll() {
        Session session = getFactory().getCurrentSession();
        String[] tables = new String[] { "HoldFeature", "ReleasePressFeature", "PressPressFeature", "Feature" };
        for (int i = 0; i < tables.length; i++) {
            String hql = String.format("DELETE FROM %s", tables[i]);
            Query query = session.createQuery(hql);
            query.executeUpdate();
        }
    }

}
