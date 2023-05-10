# Sim-Piece
*Sim-Piece: Highly Accurate Piecewise Linear Approximation through Similar Segment Merging*

The source code of the Sim-Piece publication is available [here](https://github.com/xkitsios/Sim-Piece/releases/tag/Paper_Edition). 

## Test
Execute (Requires Java SE 8+):

```
mvn test -Dtest=TestSimPiece
```

### Error thresholds 𝜖 (Epsilon)
Results for 0.5% × 𝑟𝑎𝑛𝑔𝑒 ≤ 𝜖 ≤ 5% × 𝑟𝑎𝑛𝑔𝑒

The 𝑟𝑎𝑛𝑔𝑒 is defined as the difference between the maximum and the minimum value of a signal.

### Metrics
- Compression Ratio
- Execution Time


### PLA Methods
- [Sim-Piece](http://vldb.org/pvldb/volumes/16/paper/Sim-Piece%3A%20Highly%20Accurate%20Piecewise%20Linear%20Approximation%20through%20Similar%20Segment%20Merging)
- [Swing](https://dl.acm.org/doi/10.14778/1687627.1687645)
- [PMCMR](https://ieeexplore.ieee.org/document/1260811)


### Datasets

- [Cricket](https://www.cs.ucr.edu/~eamonn/time_series_data_2018)
- [FaceFour](https://www.cs.ucr.edu/~eamonn/time_series_data_2018)
- [Lighting](https://www.cs.ucr.edu/~eamonn/time_series_data_2018)
- [MoteStrain](https://www.cs.ucr.edu/~eamonn/time_series_data_2018)
- [Wafer](https://www.cs.ucr.edu/~eamonn/time_series_data_2018)
- [Wind Speed](https://data.neonscience.org/data-products/DP1.20059.001/RELEASE-2022)
- [Wind Direction](https://data.neonscience.org/data-products/DP1.20059.001/RELEASE-2022)
- [Pressure](https://data.neonscience.org/data-products/DP1.20004.001/RELEASE-2022)

### Example Output
>/Cricket.csv.gz  
>Sim-Piece  
>Epsilon: 0.50%	Compression Ratio: 9.233	Execution Time: 388ms  
>Epsilon: 1.00%	Compression Ratio: 15.722	Execution Time: 118ms  
>Epsilon: 1.50%	Compression Ratio: 22.475	Execution Time: 56ms  
>Epsilon: 2.00%	Compression Ratio: 29.393	Execution Time: 37ms  
>Epsilon: 2.50%	Compression Ratio: 36.440	Execution Time: 25ms  
>Epsilon: 3.00%	Compression Ratio: 43.751	Execution Time: 23ms  
>Epsilon: 3.50%	Compression Ratio: 51.236	Execution Time: 20ms  
>Epsilon: 4.00%	Compression Ratio: 58.691	Execution Time: 20ms  
>Epsilon: 4.50%	Compression Ratio: 66.852	Execution Time: 15ms  
>Epsilon: 5.00%	Compression Ratio: 74.790	Execution Time: 14ms  
>...  
>Sim-Piece Variable Encoding  
>Epsilon: 0.50%	Compression Ratio: 13.603	Execution Time: 140ms  
>Epsilon: 1.00%	Compression Ratio: 22.877	Execution Time: 59ms  
>Epsilon: 1.50%	Compression Ratio: 32.459	Execution Time: 36ms  
>Epsilon: 2.00%	Compression Ratio: 42.174	Execution Time: 29ms  
>Epsilon: 2.50%	Compression Ratio: 52.160	Execution Time: 29ms  
>Epsilon: 3.00%	Compression Ratio: 62.372	Execution Time: 23ms  
>Epsilon: 3.50%	Compression Ratio: 72.868	Execution Time: 18ms  
>Epsilon: 4.00%	Compression Ratio: 83.129	Execution Time: 16ms  
>Epsilon: 4.50%	Compression Ratio: 94.972	Execution Time: 15ms  
>Epsilon: 5.00%	Compression Ratio: 105.932	Execution Time: 16ms  
Sim-Piece Variable Encoding & ZStd  
>Epsilon: 0.50%	Compression Ratio: 14.632	Execution Time: 107ms  
>Epsilon: 1.00%	Compression Ratio: 24.634	Execution Time: 55ms  
>Epsilon: 1.50%	Compression Ratio: 34.978	Execution Time: 35ms  
>Epsilon: 2.00%	Compression Ratio: 45.417	Execution Time: 30ms  
>Epsilon: 2.50%	Compression Ratio: 56.140	Execution Time: 24ms  
>Epsilon: 3.00%	Compression Ratio: 67.224	Execution Time: 20ms  
>Epsilon: 3.50%	Compression Ratio: 78.482	Execution Time: 18ms  
>Epsilon: 4.00%	Compression Ratio: 89.479	Execution Time: 18ms  
>Epsilon: 4.50%	Compression Ratio: 102.174	Execution Time: 18ms  
>Epsilon: 5.00%	Compression Ratio: 113.636	Execution Time: 15ms  
>Swing  
>Epsilon: 0.50%	Compression Ratio: 3.113	Execution Time: 68ms  
>Epsilon: 1.00%	Compression Ratio: 5.361	Execution Time: 25ms  
>Epsilon: 1.50%	Compression Ratio: 7.585	Execution Time: 13ms  
>Epsilon: 2.00%	Compression Ratio: 9.811	Execution Time: 13ms  
>Epsilon: 2.50%	Compression Ratio: 12.054	Execution Time: 12ms  
>Epsilon: 3.00%	Compression Ratio: 14.268	Execution Time: 12ms  
>Epsilon: 3.50%	Compression Ratio: 16.506	Execution Time: 12ms  
>Epsilon: 4.00%	Compression Ratio: 18.677	Execution Time: 13ms  
>Epsilon: 4.50%	Compression Ratio: 20.683	Execution Time: 12ms  
>Epsilon: 5.00%	Compression Ratio: 22.810	Execution Time: 15ms  
>PMCMR  
>Epsilon: 0.50%	Compression Ratio: 2.066	Execution Time: 38ms  
>Epsilon: 1.00%	Compression Ratio: 3.253	Execution Time: 22ms  
>Epsilon: 1.50%	Compression Ratio: 4.541	Execution Time: 9ms  
>Epsilon: 2.00%	Compression Ratio: 5.888	Execution Time: 11ms  
>Epsilon: 2.50%	Compression Ratio: 7.308	Execution Time: 10ms  
>Epsilon: 3.00%	Compression Ratio: 8.791	Execution Time: 6ms  
>Epsilon: 3.50%	Compression Ratio: 10.332	Execution Time: 8ms  
>Epsilon: 4.00%	Compression Ratio: 11.959	Execution Time: 7ms  
>Epsilon: 4.50%	Compression Ratio: 13.659	Execution Time: 8ms  
>Epsilon: 5.00%	Compression Ratio: 15.438	Execution Time: 9ms  
>...

## Notes
- It is recommended to use Sim-Piece with Variable Byte encoding.
- Execution time may vary depending on hardware specifications and other factors.

## Who do I talk to?
- [Xenophon Kitsios](https://xkitsios.github.io/)
- [Panagiotis Liakos](https://cgi.di.uoa.gr/~p.liakos/)
- [Katia Papakonstantinopoulou](https://www2.aueb.gr/users/katia/)
- [Yannis Kotidis](http://pages.cs.aueb.gr/~kotidis/)
