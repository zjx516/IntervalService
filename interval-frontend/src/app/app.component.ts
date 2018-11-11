import { Component } from '@angular/core';

import { MeterService } from './meter.service';
import { Metric } from './metric';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'My First Angular App!';

  metric: Metric;
  error: any;
  loaded: boolean;
  triggerFlag: boolean;
  
  constructor(
    private meterService: MeterService,
  ) {
    this.triggerFlag = true;
  }

  trigger() {
    this.meterService.trigger()
    .subscribe(
      error => (this.error = error)
    );
    if (this.error == null) {
      this.triggerFlag =  false;
    }
    setInterval(() => {
      this.getMetric()
    }, 1000);
  }

  getMetric() {
    this.meterService
    .getMetric()
    .subscribe(
      metric => (this.metric = metric),
      error => (this.error = error)
    );
    if (this.metric != null) {
      this.loaded = true;
    }
  }
}

