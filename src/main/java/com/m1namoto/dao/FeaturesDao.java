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

    public List<HoldFeature> getHoldFeatures() {
        String hql = "FROM HoldFeature ORDER BY code";
        Query query = getFactory().openSession().createQuery(hql);

        return query.list();
    }

    public List<ReleasePressFeature> getReleasePressFeatures() {
        String hql = "FROM ReleasePressFeature ORDER BY releaseCode, pressCode";
        Query query = getFactory().openSession().createQuery(hql);

        return query.list();
    }
    
    public List<Feature> getUserFeatures(User user) {
        String hql = "FROM Feature WHERE user_id = :user_id";
        Session session = getFactory().openSession();
        Query query = session.createQuery(hql);
        query.setParameter("user_id", user.getId());
        List<Feature> features = query.list(); 
        session.close();
        
        return features;
    }
    
    public void deleteFeatures(User user) {
        List<Feature> features = getUserFeatures(user);
        for (Feature feature : features) {
            delete(feature);
        }
    }

}
