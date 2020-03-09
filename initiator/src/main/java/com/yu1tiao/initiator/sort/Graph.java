package com.yu1tiao.initiator.sort;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Vector;

/**
 * 有向无环图的拓扑排序算法
 */
class Graph {
    //顶点数
    private int mVerticesCount;
    //邻接表
    private List<Integer>[] mAdj;

    Graph(int verticesCount) {
        this.mVerticesCount = verticesCount;
        mAdj = new ArrayList[mVerticesCount];
        for (int i = 0; i < mVerticesCount; i++) {
            mAdj[i] = new ArrayList<>();
        }
    }

    /**
     * 添加边
     *
     * @param u from
     * @param v to
     */
    void addEdge(int u, int v) {
        mAdj[u].add(v);
    }

    /**
     * 拓扑排序，先建立入度表和邻接表，找出入度为0的顶点，然后删除，并将它的邻接点入度-1，
     * 重复此过程完成拓扑排序，如果不存在入度为0的点，则说明有环，不能进行拓扑排序
     */
    Vector<Integer> topologicalSort() {
        int[] inDegree = new int[mVerticesCount];
        for (int i = 0; i < mVerticesCount; i++) {//初始化所有点的入度数量
            ArrayList<Integer> temp = (ArrayList<Integer>) mAdj[i];
            for (int node : temp) {
                inDegree[node]++;
            }
        }
        Queue<Integer> queue = new LinkedList<Integer>();
        for (int i = 0; i < mVerticesCount; i++) {//找出所有入度为0的点
            if (inDegree[i] == 0) {
                queue.add(i);
            }
        }
        int cnt = 0;
        Vector<Integer> topOrder = new Vector<Integer>();
        while (!queue.isEmpty()) {
            int u = queue.poll();
            topOrder.add(u);
            for (int node : mAdj[u]) {//找到该点（入度为0）的所有邻接点
                if (--inDegree[node] == 0) {//把这个点的入度减一，如果入度变成了0，那么添加到入度0的队列里
                    queue.add(node);
                }
            }
            cnt++;
        }
        if (cnt != mVerticesCount) {//检查是否有环，理论上拿出来的点的次数和点的数量应该一致，如果不一致，说明有环
            throw new IllegalStateException("Exists a cycle in the graph");
        }
        return topOrder;
    }
}
