package com.interview.jiaxin.v1.interval.service;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.interview.jiaxin.v1.interval.ProcessingMonitor;
import com.interview.jiaxin.v1.interval.pojo.DailyMeterMaximum;
import com.interview.jiaxin.v1.interval.response.ApiResponse;
import com.interview.jiaxin.v1.interval.response.MeterIntervalData;
import com.interview.jiaxin.v1.interval.response.MeterIntervalValueData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.*;
import java.util.*;
import java.util.concurrent.*;

import static com.interview.jiaxin.v1.interval.constant.Constant.*;


@Service
public class MeterProcessService {
	private static final Logger logger = LoggerFactory.getLogger(MeterProcessService.class);

	@Value("${token}")
	private String token;

	private static ThreadFactory builder = new ThreadFactoryBuilder()
			.setNameFormat("Interval value request Task Executor #%d").build();

	private ExecutorService executor = new ThreadPoolExecutor(5,
			20,
			100,
			TimeUnit.MILLISECONDS,
			new ArrayBlockingQueue<>(10),
			builder,
			new ThreadPoolExecutor.CallerRunsPolicy());

	/**
	 *  Get all meters with available interval duration
 	 */
	public List<MeterIntervalData> getAllAvailableMeters() {
		List<MeterIntervalData> result = Lists.newArrayList();
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<ApiResponse<MeterIntervalData>> responseEntity = restTemplate
				.exchange(METER_LIST_URL,
						HttpMethod.GET,
						null,
						new ParameterizedTypeReference<ApiResponse<MeterIntervalData>>() {
						});

		if (Objects.nonNull(responseEntity.getBody())) {
			result.addAll(responseEntity.getBody().getData());
		}
		return result;
	}

	public void maximumValue(MeterIntervalData data) {
		Map<String, Double> resultMap = new HashMap<>();
		LocalDateTime startDateTime = LocalDateTime.ofInstant(Instant.parse(data.getEarliestDttm()), ZoneOffset.UTC);
		LocalDateTime endDateTime = LocalDateTime.ofInstant(Instant.parse(data.getLatestDttm()), ZoneOffset.UTC);

		LocalDateTime start = startDateTime;
		List<Future<DailyMeterMaximum>> futures = new ArrayList<>();

		// Concurrently requesting interval values by batch in hourly from remote server
		while (endDateTime.isAfter(start)) {
			resultMap.putIfAbsent(start.toLocalDate().toString(), null);
			LocalDateTime end = start.plusHours(1);

			if (endDateTime.isBefore(end)) {
				end = endDateTime;
			}

			// Latest time of day
			LocalDateTime endDay = LocalDateTime.of(LocalDate.from(start), LocalTime.MAX);

			if (endDay.isBefore(end)) {
				end = endDay;
				futures.add(executor.submit(new IntervalValueRequest(data.getMeterId(), start, end)));

				// Earliest time of day
				start = LocalDateTime.of(LocalDate.from(start.plusDays(1)), LocalTime.MIN);
			} else {
				futures.add(executor.submit(new IntervalValueRequest(data.getMeterId(), start, end)));
				start = end;
			}
		}

		for (Future<DailyMeterMaximum> result : futures) {
			try {
				DailyMeterMaximum dailyMeterMaximum = result.get();
				String day = dailyMeterMaximum.getDay();

				// Update maximum value if needed
				if (resultMap.get(day) == null || resultMap.get(day) < dailyMeterMaximum.getMaximum_value()) {
					resultMap.put(day, dailyMeterMaximum.getMaximum_value());
				}
			} catch (InterruptedException | ExecutionException e) {
				logger.error("Error happened while requesting interval values ", e);
			}

		}

		for (Map.Entry<String, Double> entry : resultMap.entrySet()) {
			this.upload(data.getMeterId(), entry.getKey(), entry.getValue());
		}
	}

	private void upload(String meterId, String date, Double maximum) {
		DailyMeterMaximum meterMaximum = new DailyMeterMaximum(meterId, date, maximum);
		RestTemplate template = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.add("token", token);
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<DailyMeterMaximum> request = new HttpEntity<>(meterMaximum, headers);


		ResponseEntity<ApiResponse> responseEntity = template.postForEntity(DAILY_MAXIMUM_SUBMIT_URL,
				request, ApiResponse.class);

		if (responseEntity.getStatusCode().equals(HttpStatus.OK)) {
			long submitted = ProcessingMonitor.increaseAndGetSubmittedNum();
			logger.trace("Has submitted {} meters", submitted);
		}
	}

	class IntervalValueRequest implements Callable<DailyMeterMaximum> {
		private HttpEntity<MeterIntervalData> request;
		private RestTemplate template;
		private DailyMeterMaximum result;
		private UriComponentsBuilder builder;

		IntervalValueRequest(String meterId, LocalDateTime start, LocalDateTime end) {
			this.result = new DailyMeterMaximum(meterId, start.toLocalDate().toString(), Double.MIN_VALUE);
			template = new RestTemplate();
			HttpHeaders requestHeaders = new HttpHeaders();
			requestHeaders.add("token", token);

			request = new HttpEntity<>(null, requestHeaders);

			builder = UriComponentsBuilder.fromHttpUrl(METER_INTERVAL_VALUE_URL)
					.queryParam("meter_id", meterId)
					.queryParam("start_dttm", start.atZone(ZoneOffset.UTC))
					.queryParam("end_dttm", end.atZone(ZoneOffset.UTC));
		}

		@Override
		public DailyMeterMaximum call() throws Exception {
			ResponseEntity<ApiResponse<MeterIntervalValueData>> response =
					template.exchange(builder.build().encode().toUri(),
							HttpMethod.GET,
							request,
							new ParameterizedTypeReference<ApiResponse<MeterIntervalValueData>>() {});

			if (Objects.isNull(response.getBody())) {
				return null;
			}

			for (MeterIntervalValueData data : response.getBody().getData()) {
				// adding num of processed intervals
				long processed = ProcessingMonitor.increaseAndGetProcessedNum();
				logger.trace("Processed values num: {}", processed);
				if (data.getValue() > result.getMaximum_value()) {
					result.setMaximum_value(data.getValue());
				}
			}
			return result;
		}
	}
}
