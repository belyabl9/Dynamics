package com.m1namoto.dao;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.m1namoto.domain.Feature;
import com.m1namoto.domain.HoldFeature;
import com.m1namoto.domain.ReleasePressFeature;
import com.m1namoto.domain.User;

public class FeaturesDao extends GenericDAO<Feature> {

    public FeaturesDao(SessionFactory factory) {
        super(Feature.class, factory);
    }

    /**
     * Returns a list of hold features
     * @return List of hold features
     */
    @SuppressWarnings("unchecked")
    public List<HoldFeature> getHoldFeatures() {
        String hql = "FROM HoldFeature ORDER BY code";
        Session session = getFactory().getCurrentSession();
        Query query = session.createQuery(hql);
        List<HoldFeature> features = query.list();

        return features;
    }

    /**
     * Returns a list of release-press features
     * @return List of release-press features
     */
    @SuppressWarnings("unchecked")
    public List<ReleasePressFeature> getReleasePressFeatures() {
        String hql = "FROM ReleasePressFeature ORDER BY releaseCode, pressCode";
        Session session = getFactory().getCurrentSession();
        Query query = session.createQuery(hql);
        List<ReleasePressFeature> features = query.list();

        return features;
    }

    /**
     * Returns a list of user features
     * @param user
     * @return List of user features
     */
    @SuppressWarnings("unchecked")
    public List<Feature> getUserFeatures(User user) {
        String hql = "FROM Feature WHERE user_id = :user_id";
        Session session = getFactory().getCurrentSession();
        Query query = session.createQuery(hql);
        query.setParameter("user_id", user.getId());
        List<Feature> features = query.list(); 
        
        return features;
    }

    /**
     * Returns a list of user hold features
     * @param user
     * @return List of user hold features
     */
    @SuppressWarnings("unchecked")
    public List<HoldFeature> getUserHoldFeatures(User user) {
        String hql = "FROM HoldFeature WHERE user_id = :user_id";
        Session session = getFactory().getCurrentSession();
        Query query = session.createQuery(hql);
        query.setParameter("user_id", user.getId());
        List<HoldFeature> features = query.list(); 
        
        return features;
    }
    
    /**
     * Returns a list of user release-press features
     * @param user
     * @return List of user release-press features
     */
    @SuppressWarnings("unchecked")
    public List<ReleasePressFeature> getUserReleasePressFeatures(User user) {
        String hql = "FROM ReleasePressFeature WHERE user_id = :user_id";
        Session session = getFactory().getCurrentSession();
        Query query = session.createQuery(hql);
        query.setParameter("user_id", user.getId());
        List<ReleasePressFeature> features = query.list(); 

        return features;
    }
    
    /**
     * Returns a list of session features
     * @param session
     * @return List of session features
     */
    @SuppressWarnings("unchecked")
    public List<Feature> getSessionFeatures(com.m1namoto.domain.Session session) {
        String hql = "FROM Feature WHERE session_id = :session_id";
        Session hibSession = getFactory().getCurrentSession();
        Query query = hibSession.createQuery(hql);
        query.setParameter("session_id", session.getId());
        List<Feature> features = query.list(); 
        
        return features;
    }
    
    /**
     * Deletes user features
     * @param user
     */
    public void deleteFeatures(User user) {
        List<Feature> features = getUserFeatures(user);
        for (Feature feature : features) {
            delete(feature);
        }
    }
    
    /**
     * Deletes all features
     */
    public void deleteAll() {
        Session session = getFactory().getCurrentSession();
        String[] tables = new String[] { "HoldFeature", "ReleasePressFeature", "Feature" };
        for (int i = 0; i < tables.length; i++) {
            String hql = String.format("DELETE FROM %s", tables[i]);
            Query query = session.createQuery(hql);
            query.executeUpdate();
        }
    }

}
