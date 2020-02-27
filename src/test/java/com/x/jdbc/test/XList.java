package com.x.jdbc.test;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.alibaba.fastjson.annotation.JSONField;


public class XList<T> implements List<T>{
    private List<T> list;
    @JSONField(serialize = false)
    Update update;

    public XList(List<T> list, Update update) {
        this.list = list;
        this.update = update;
    }

    public XList(Update update) {
        this.list = new ArrayList<>();
        this.update = update;
    }

    public XList(Object obj) {
        if (obj instanceof Update){
           this.update = (Update)obj;
        }
        this.list = new ArrayList<>();
    }

    public XList(List<T> list, Object obj) {
        if (obj instanceof Update){
            this.update = (Update)obj;
        }
        this.list = list;
    }

    public XList() {
        this.list = new ArrayList<>();
    }

    public XList(List<T> list){
        this.list = list;
    }

    public boolean setUpdate(Object obj){
        if (obj instanceof Update && update == null){
            this.update = (Update)obj;
            return true;
        }
        return  false;
    }
    @Override
    public ListIterator<T> listIterator() {
        return list.listIterator();
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return list.contains(o);
    }

    @Override
    public Iterator<T> iterator() {
        return list.listIterator();
    }

    @Override
    public Object[] toArray() {
        return list.toArray();
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        return list.toArray(a);
    }

    @Override
    public boolean add(T t) {
        boolean add = list.add(t);
        update();

        return add;
    }

    @Override
    public boolean remove(Object o) {
        boolean remove = list.remove(o);
        update();

        return remove;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return list.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        boolean b = list.addAll(c);
        update();

        return b;
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        boolean b = list.addAll(index, c);
        update();
        return b;
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean b = list.removeAll(c);
        update();
        return b;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        boolean b = list.retainAll(c);
        update();

        return b;
    }

    @Override
    public void clear() {
        list.clear();
        update();

    }

    @Override
    public T get(int index) {
        return list.get(index);
    }

    @Override
    public T set(int index, T element) {
        T set = list.set(index, element);
        update();

        return set;
    }

    @Override
    public void add(int index, T element) {
        list.add(index, element);
        update();
    }

    @Override
    public T remove(int index) {
        T remove = list.remove(index);
        update();
        return remove;
    }

    @Override
    public int indexOf(Object o) {
        return list.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return list.lastIndexOf(o);
    }


    @Override
    public ListIterator<T> listIterator(int index) {
        return list.listIterator(index);
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        List<T> ts = list.subList(fromIndex, toIndex);
        if (update != null){
            update.update();
        }
        return ts;
    }

    @Override
    public String toString() {
        return "XList{" + "list=" + list + '}';
    }

    private void update(){
        if (update != null){
            update.update();
        }
    }

}
