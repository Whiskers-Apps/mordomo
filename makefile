DEST_DIR := /opt/mordomo
DIST_DIR := desktopApp/build/compose/binaries/main/app/mordomo

distributable:
	./gradlew :desktopApp:createDistributable

install: distributable
	sudo cp -a "$(DIST_DIR)/." "$(DEST_DIR)/"
	@echo "Installed to $(DEST_DIR)"

kill:
	killall java || true
	rm /tmp/mordomo.port || true

reload:	kill
	./gradlew desktopApp:run
