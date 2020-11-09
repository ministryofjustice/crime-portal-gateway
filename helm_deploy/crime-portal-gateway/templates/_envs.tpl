    {{/* vim: set filetype=mustache: */}}
{{/*
Environment variables for web and worker containers
*/}}
{{- define "deployment.envs" }}
env:
  - name: SERVER_PORT
    value: "{{ .Values.image.port }}"

  - name: JAVA_OPTS
    value: "{{ .Values.env.JAVA_OPTS }}"

  - name: SPRING_PROFILES_ACTIVE
    value: "logstash"

  - name: APPINSIGHTS_INSTRUMENTATIONKEY
    valueFrom:
      secretKeyRef:
        name: crime-portal-gateway-secrets
        key: APPINSIGHTS_INSTRUMENTATIONKEY

  - name: KEYSTORE_PASSWORD
    valueFrom:
      secretKeyRef:
        name: crime-portal-gateway-secrets
        key: KEYSTORE_PASSWORD

{{/*  - name: KEYSTORE*/}}
{{/*    valueFrom:*/}}
{{/*      secretKeyRef:*/}}
{{/*        name: crime-portal-gateway-keystore-cert*/}}
{{/*        key: keystore.jks*/}}

{{- end -}}
