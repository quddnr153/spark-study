# Spark study

- 책 이름: 빅데이터 분석을 위한 스파크 2 프로그래밍 (대용량 데이터 처리부터 머신러닝까지)
- 책 주소: http://wikibook.co.kr/spark2nd/
- 책 예제: https://github.com/wikibook/spark

## Prerequisite

- Mac, Linux, or Windows (OS)

***메모리***

스파크에서는 전체 메모리의 75% 정도만 스파크에 할당하고 나머지는 운영체제와 버퍼 캐시 용도로 사용할 것을 권장한다.

메모리의 경우 하나의 프로세스에 지나치게 높은 메모리를 할당할 경우 가비지 컬렉션 (garbege collection) 의 영향력을 높여 오히려 성능 저하를 가져올 수 있으므로 전체 프로세스 개수와 더불어 적절히 분배할 필요가 있다.

***CPU***

CPU 코어는 서버당 8 ~ 16 코어 정도가 적당하다. 스파크 애플리케이션은 하나의 작업을 익스큐터 (executor) 라고 하는 여러 개의 프로세스를 생성해서 병렬로 처리하는데 이때 익스큐터별로 서로 다른 CPU 코어를 사용하므로 CPU 코어의 개수가 많을수록 병렬처리에 더 유리하다고 할 수 있다.

- RDD, Dataframe, Dataset

차이가 무엇일까?

## 02 RDD

### Intro

1. Spark cluster

cluster: 여러 대의 서버가 마치 한 대의 서버처럼 동작하는 것

- 스파크 역시 클러스터 환경에서 동작
- 대량의 데이터를 여러 서버로 나누어 병렬로 처리

2. 분산 데이터로서의 RDD

- Resilient Distributed Datasets 의 약자
- Resilient = 회복가능한 -> 복구
- 복구의 의미: 작업 수행 도중 서버나 네트워크, 자원 할당 등에 일시적/부분적 문제가 발생했을 때 RDD 의 작업 히스토리를 이용한 재시도를 수행함으로써 복구를 수행할 수 있다는 뜻
- RDD 는 다수의 element 를 포함
- elements 는 다시 일정한 단위의 작은 집합으로 나눠져 앞에서 설명한 스파크 클러스터에 흩어져서 저장

3. RDD 의 불변성

한번 만들어진 RDD 는 어떤 경우에도 그 내용이 변경되지 않기 때문에 같은 방법으로 만든 RDD 는 항상 같은 데이터를 갖게 된다.

4. 파티션 (Partition)

- RDD 데이터는 클러스터를 구성하는 여러 서버에 나누어 저장
- 분할된 데이터를 파티션이라는 단위로 관리
- HDFS 를 사용한다면 하나의 HDFS 블록에 하나의 파티션이 구성
- 파티션의 크기를 조정하는 것은 애플리케이션 성능에 큰 영향을 주므로 스파크의 동작 특성을 잘 이해하고 적절한 크기로 파티션 수를 설정하는 것이 매우 중요

5. HDFS

- 스파크는 하둡과 밀접한 관계
- 스파크는 하둡의 파일 입출력 API 에 의존성을 가지고 있기에 하둡 파일시스템과 관련 API 에 대한 기본적인 이해가 필요

6. Job and Executor

- 스파크 프로그램을 실행하는 것을 스파크 잡 (Job) 을 실행한다고 한다
- 각 서버마다 익스큐터 (executor) 라는 프로세스가 생성 되고 이를 통해 각자 할당된 파티션을 처리한다

7. 드라이버 프로그램

- 드라이버란 스파크컨텍스트를 생성하고 그 인스턴스를 포함하고 있는 프로그램을 의미

드라이버 프로그램은 자신을 실행한 서버에서 동작하면서 스파크컨텍스트를 생성해 클러스터의 각 워커 노드들에게 작업을 지시하고 결과를 취합하는 역할을 수행.

일반적으로 드라이버 프로그램을 실행하는 서버는 스파크 클러스터를 구성하지 않는 서버, 즉 별도의 작업용 서버를 사용하는 경우가 많음.

8. Transformation and Action

- RDD 가 제공하는 Operation 은 Transformation 과 Action 으로 나뉨

간략히 연산의 수행 결과가 RDD 인 경우 Transformation 이라 하고 이외에는 Action 이라 할 수 있음

9. Lazy 동작과 최적화

Transformation 에 해당하는 연산인 경우 해당 RDD 를 사용하는 다른 Action 연산이 호출될 때까지는 실제 Transformation 을 수행하지 않는 방식으로 동작

```spark
val rdd = sc.parallelize(List(1, 2, 3, 4, 5, 6, 7))

// 연산을 하지 않음
val newRdd = rdd.map(_ + 1).filter(_ % 2 == 0)

// Action 연산이 호출되면서 위 Transformation operation 이 동작
newRdd.collect().foreach(println)
```

- 실행 계획의 최적화 (Locality)가 가능

사용자가 입력한 변환 연산들을 즉시 수행하지 않고 모아뒀다가 한번에 실행함으로써 불필요한 네트워크 통신 비용을 줄일 수 있음

### RDD 기본 Action

- collect

collect 는 RDD 의 모든 원소를 모아서 배열로 변환.

collect 연산을 호출한 서버의 메모리에 수집, 따라서 전체 데이터를 모두 담을 수 있을 정도의 충분한 메모리 공간이 확보돼 있는 상태에서만 사용해야 함.

```scala
val rdd = sc.parallelize(1 to 10)
val result = rdd.collect
println(result.mkString(", "))

```

- count

count 는 RDD 를 구성하는 전체 요소의 개수를 반환

```scala
val rdd = sc.parallelize(1 to 10)
val result = rdd.count
println(result)
```

### RDD Transformation

Transformation 은 기존 RDD 를 이용해 새로운 RDD 를 생성하는 연산

자주사용하는 연산:

- Map: 요소 간의 mapping 을 정의한 함수를 RDD 에 속하는 모든 요소에 적용해 새로운 RDD 를 생성
- Group: 특정 조건에 따라 요소를 그룹화하거나 특정 함수를 적용
- Aggregation: RDD 에 포함된 요소를 하나의 집합으로 간주할 때 서로 다른 RDD 간에 합집합, 교집합 등을 계산
- Partition: RDD 의 파티션 개수를 조정
- Filter and Sort: 특정 조건을 만족하는 요소만 선택하거나 각 요소를 정해진 기준에 따라 정렬

#### map 관련 operations

##### map

- map() 은 하나의 입력을 받아 하나의 값을 돌려주는 함수를 argument (인자) 로 받음
- map() 은 함수를 RDD 에 속하는 모든 요소에 적용한 뒤 그 결과를 구성된 새로운 RDD 를 생성해 return
