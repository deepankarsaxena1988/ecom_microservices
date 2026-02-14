# Use the official Grafana image as the base image
FROM grafana/grafana-oss:latest

# Optional: Install additional plugins (example for a specific plugin)
# Uncomment and replace 'grafana-plugin-name' with the actual plugin ID
# RUN grafana-cli plugins install grafana-plugin-name

# Optional: Copy custom configuration files
# COPY ./custom-config.ini /etc/grafana/grafana.ini

# Optional: Copy provisioning files for datasources and dashboards
# COPY ./provisioning/datasources /etc/grafana/provisioning/datasources
# COPY ./provisioning/dashboards /etc/grafana/provisioning/dashboards

# Expose port 3000 (default Grafana port)
EXPOSE 3000