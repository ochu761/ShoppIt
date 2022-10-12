package database;

import database.models.FoodItem;
import database.models.Item;
import database.models.ShoppingList;
import jakarta.persistence.NoResultException;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class DatabaseManager {
    private Session session;

    public DatabaseManager() {
        Configuration con = new Configuration().configure().addAnnotatedClass(FoodItem.class).addAnnotatedClass(Item.class).addAnnotatedClass(ShoppingList.class);
        ServiceRegistry registry = new StandardServiceRegistryBuilder().applySettings(con.getProperties()).build();
        SessionFactory sessionFactory = con.buildSessionFactory(registry);
        session = sessionFactory.openSession();

    }

    public void addObject(Object o) {
        Transaction tx = session.beginTransaction();
        session.persist(o);
        tx.commit();
    }

    public void updateObject(Object o) {
        Transaction tx = session.beginTransaction();
        session.merge(o);
        tx.commit();
    }

    /**
     * Delete an item of a given name from the database
     * @param itemName Name of item to delete
     */
    public void deleteItemByName(String itemName) {
        FoodItem foodItem = getFromDatabase(FoodItem.class,
                "SELECT f FROM FoodItem f WHERE f.productName='" + itemName + "'").get(0);

        // Did not find item of name itemName
        if (foodItem == null) {
            return;
        }

        deleteObjectById(foodItem.getId());
    }

    /**
     * Delete an object with a known id from the database
     * @param id ID of object to delete
     */
    public void deleteObjectById(int id) {
        Transaction tx = session.beginTransaction();
        Object object = session.get(FoodItem.class, id);
        session.remove(object);
        tx.commit();
    }

    /**
     * Get a table
     * @param targetClass Class type to be returned
     * @param tableName Table name
     * @return List of attributes of type targetClass from table
     * @param <T>
     */
    public <T> List<T> getTable(Class<T> targetClass, String tableName) {
        return getFromDatabase(targetClass, "FROM " + tableName);
    }

    /**
     * Get attributes from a table
     * @param targetClass Class type to be returned
     * @param attribute Attribute from the table
     * @param tableName Table name
     * @return List of attributes of type targetClass from table
     * @param <T>
     */
    public <T> List<T> getAttributeList(Class<T> targetClass, String attribute, String tableName) {
        return getFromDatabase(targetClass, "SELECT s." + attribute + " FROM " + tableName + " s");
    }

    /**
     * @param targetClass
     * @param HQLQuery    For getting all items of a particular class use "FROM classname"
     * @param <T>
     * @return
     */
    private <T> List<T> getFromDatabase(Class<T> targetClass, String HQLQuery) {
        Transaction tx = null;
        List<T> list;

        try {
            tx = session.beginTransaction();
            list = session.createQuery(HQLQuery, targetClass).list();
            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) tx.rollback();
            e.printStackTrace();
            list = new ArrayList<>();
        }
        return list;
    }

    public <T> List<T> getAllFromDataBase(Class<T> targetClass) {
        return getFromDatabase(targetClass, "FROM ".concat(targetClass.getSimpleName()));
    }

    public void updateImage(String foodName, String update) {
        List<FoodItem> foodItemObjects = session.createQuery("select f FROM FoodItem f", FoodItem.class).list();
        for (FoodItem item : foodItemObjects) {
            String name = item.getProductName();
            if (name.equals(foodName)) {
                item.setImgFilename(update);
                session.merge(item);
            }
        }
    }
}
