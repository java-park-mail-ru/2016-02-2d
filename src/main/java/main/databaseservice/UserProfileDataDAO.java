package main.databaseservice;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

import java.util.List;

public class UserProfileDataDAO {

    public UserProfileDataDAO(Session session) {
        this.session = session;
    }

    public void save(UserProfileData dataSet) {
        session.saveOrUpdate(dataSet);
    }

    public UserProfileData read(long id) {
        return session.get(UserProfileData.class, id);
    }

    public UserProfileData readByName(String name) {
        final Criteria criteria = session.createCriteria(UserProfileData.class);
        return (UserProfileData) criteria.add(Restrictions.eq("login", name)).uniqueResult();
    }

    @SuppressWarnings("unchecked")
    public List<UserProfileData> readAll() {
        final Criteria criteria = session.createCriteria(UserProfileData.class);
        return (List<UserProfileData>) criteria.list();
    }

    public void delete(long id) {
        final UserProfileData dataSet = new UserProfileData();
        dataSet.setId(id);
        final Transaction transaction = session.beginTransaction();
        session.delete(dataSet);
        transaction.commit();
    }


    private final Session session;

}
