package cn.andios.decorator;

/**
 * @description:
 * @author:LSD
 * @when:2020/04/17/16:12
 */
public class ConcreteDecorator1 extends Decorator{

    public ConcreteDecorator1(Component component) {
        super(component);
    }

    @Override
    public void doSomething() {
        super.doSomething();
        this.doAnotherThing();
    }

    private void doAnotherThing() {
        System.out.println("功能B");
    }
}
