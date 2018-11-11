import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, throwError as observableThrowError } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

import { Metric } from './metric';

@Injectable()
export class MeterService {
  private metricUrl = 'http://localhost:4200/v1/metric'; // URL to web api
  private triggerUrl = 'http://localhost:4200/v1/trigger'; // URL to web api


  constructor(private http: HttpClient) {}

  trigger() {
    const headers = new Headers();
    headers.append('Content-Type', 'application/json');
    return this.http.get<Metric>(this.triggerUrl)
      .pipe(map(data => data), catchError(this.handleError));
  }

  getMetric() {
    const headers = new Headers();
    headers.append('Content-Type', 'application/json');

    return this.http.get<Metric>(this.metricUrl)
      .pipe(map(data => data), catchError(this.handleError));
  }

  private handleError(res: HttpErrorResponse | any) {
    console.error(res.error || res.body.error);
    return observableThrowError(res.error || 'Server error');
  }
}
