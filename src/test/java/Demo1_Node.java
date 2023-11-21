
/**
 * @ClassName:
 * @Author: mengwenting
 * @Date: 2023/8/25 11:46
 * @Description:
 * @update:
 */
public class Demo1_Node {
    //链表头节点
//    private Node head;
    //链表尾节点
//    private Node last;
    //链表之际长度
    private int size;

    //链表中插入元素
    /**
     * @param data: 链表插入元素
     * @param index: 链表插入位置
     */

    //链表插入元素
    public void insert(int data, int index) throws Exception{
        if (index < 0 || index > size){
            throw new IndexOutOfBoundsException("数组越界异常");
        }
//        Node insertedNode = new Node(data);
//        插入头部
        if (size == 0){
//            head = insertedNode;
//            last = insertedNode;
        }
        //插入尾部
        else if (index == size){
//            last = insertedNode;

        }
        //插入中间
        else if (0 < index && index < size){
            //后边的元素向后移动

        }
    }
}
