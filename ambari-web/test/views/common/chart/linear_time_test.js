/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

var App = require('app');
require('views/common/chart/linear_time');

describe('App.ChartLinearTimeView', function () {

  var chartLinearTimeView = App.ChartLinearTimeView.create({});

  describe('#transformData ([[1, 2], [2, 3], [3, 4]], "abc")', function () {

    var data = [[1, 2], [2, 3], [3, 4]];
    var name = 'abc';
    var result = chartLinearTimeView.transformData(data, name);
    it('"name" should be "abc" ', function () {
      expect(result.name).to.equal('abc');
    })
    it('data size should be 3 ', function () {
      expect(result.data.length).to.equal(3);
    })
    it('data[0].x should be 2 ', function () {
      expect(result.data[0].x).to.equal(2);
    })
    it('data[0].y should be 1 ', function () {
      expect(result.data[0].y).to.equal(1);
    })
  }),
  describe('#yAxisFormatter', function() {
    var tests = [
      {m:'undefined to 0',i:undefined,e:0},
      {m:'NaN to 0',i:NaN,e:0},
      {m:'0 to 0',i:'0',e:'0'},
      {m:'1000 to 1K',i:'1000',e:'1K'},
      {m:'1000000 to 1M',i:'1000000',e:'1M'},
      {m:'1000000000 to 1B',i:'1000000000',e:'1B'},
      {m:'1000000000000 to 1T',i:'1000000000000',e:'1T'},
      {m:'1048576 to 1.049M',i:'1048576',e:'1.049M'},
      {m:'1073741824 to 1.074B',i:'1073741824',e:'1.074B'}
    ];
    tests.forEach(function(test) {
      it(test.m + ' ', function () {
        expect(chartLinearTimeView.yAxisFormatter(test.i)).to.equal(test.e);
      });
    });
  }),
  describe('#checkSeries', function() {
    var tests = [
      {m:'undefined - false',i:undefined,e:false},
      {m:'NaN - false',i:NaN,e:false},
      {m:'object without data property - false',i:[{}],e:false},
      {m:'object with empty data property - false',i:[{data:[]}],e:false},
      {m:'object with invalid data property - false',i:[{data:[1]}],e:false},
      {m:'object with valid data property - true',i:[{data:[{x:1,y:1},{x:2,y:2}]}],e:true}
    ];
    tests.forEach(function(test) {
      it(test.m + ' ', function () {
        expect(chartLinearTimeView.checkSeries(test.i)).to.equal(test.e);
      });
    });
  }),
  describe('#BytesFormatter', function() {
    var tests = [
      {m:'undefined to "0 B"',i:undefined,e:'0 B'},
      {m:'NaN to "0 B"',i:NaN,e:'0 B'},
      {m:'0 to "0 B"',i:0,e:'0 B'},
      {m:'124 to "124 B"',i:124,e:'124 B'},
      {m:'1024 to "1 KB"',i:1024,e:'1 KB'},
      {m:'1536 to "1 KB"',i:1536,e:'1.5 KB'},
      {m:'1048576 to "1 MB"',i:1048576,e:'1 MB'},
      {m:'1073741824 to "1 GB"',i:1073741824,e:'1 GB'},
      {m:'1610612736 to "1.5 GB"',i:1610612736,e:'1.5 GB'}
    ];

    tests.forEach(function(test) {
      it(test.m + ' ', function () {

        expect(App.ChartLinearTimeView.BytesFormatter(test.i)).to.equal(test.e);
      });
    });
  }),
  describe('#PercentageFormatter', function() {
    var tests = [
      {m:'undefined to "0 %"',i:undefined,e:'0 %'},
      {m:'NaN to "0 %"',i:NaN,e:'0 %'},
      {m:'0 to "0 %"',i:0,e:'0 %'},
      {m:'1 to "1%"',i:1,e:'1%'},
      {m:'1.12341234 to "1.123%"',i:1.12341234,e:'1.123%'},
      {m:'-11 to "-11%"',i:-11,e:'-11%'},
      {m:'-11.12341234 to "-11.123%"',i:-11.12341234,e:'-11.123%'}
    ];
    tests.forEach(function(test) {
      it(test.m + ' ', function () {
        expect(App.ChartLinearTimeView.PercentageFormatter(test.i)).to.equal(test.e);
      });
    });
  }),
  describe('#TimeElapsedFormatter', function() {
    var tests = [
      {m:'undefined to "0 ms"',i:undefined,e:'0 ms'},
      {m:'NaN to "0 ms"',i:NaN,e:'0 ms'},
      {m:'0 to "0 ms"',i:0,e:'0 ms'},
      {m:'1000 to "1000 ms"',i:1000,e:'1000 ms'},
      {m:'120000 to "2 m"',i:120000,e:'2 m'},
      {m:'3600000 to "60 m"',i:3600000,e:'60 m'},
      {m:'5000000 to "1 hr"',i:5000000,e:'1 hr'},
      {m:'7200000 to "2 hr"',i:7200000,e:'2 hr'},
      {m:'90000000 to "1 d"',i:90000000,e:'1 d'}
    ];
    tests.forEach(function(test) {
      it(test.m + ' ', function () {
        expect(App.ChartLinearTimeView.TimeElapsedFormatter(test.i)).to.equal(test.e);
      });
    });
  })
})
