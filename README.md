
**A simple benchmark set for [Quadruple](https://github.com/m-vokhm/Quadruple)** 

Performs basic arithmetic operations: 
  - on BigDecimal with 39 significant digits, 
  - on Quadruple with instance methods (that change the instance value)
  - on Quadruple with static methods (that create new instances wit op results)
  
Operands for the operations being tested are taken from arrays pre-filled 
with random values ​​to avoid systematic measurement errors. 
The size of the arrays can be changed in the source code.

To run JMH-based benchmarks, build the project with maven and run: 'java -jar benchmarks.jar'. 

SimpleNanoBench.java executes similar measurements without JMH, using System.nanoTime()
