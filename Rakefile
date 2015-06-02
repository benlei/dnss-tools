require "fileutils"
require "inifile"
require "open-uri"
require "tmpdir"

$ini_path = "#{ENV['HOME']}/.dn"
$patch = "#{$ini_path}/patch.ini"
$pak = "#{$ini_path}/pak.ini"
$dnt = "#{$ini_path}/dnt.ini"
$static = "#{ENV['HOME']}/static"

fail "You need a #{$patch} with proper configurations set."  unless File.exists?($patch)
fail "You need a #{$pak} with proper configurations set." unless File.exists?($pak)
fail "You need a #{$dnt} with proper configurations set." unless File.exists?($dnt)
fail "Please git clone your region's static files to #{$static}" unless File.exists?($static)

puts "Loading #{$patch}"
$ini = IniFile.load($patch)

desc("Does a full update of the pak files and updates the JSON and pushing to git.")
task :default => [:update]

desc("Attempts to update the pak to the latest version")
task :update do
  fail "Cannot find version.cfg file: #{$static}/version.cfg" unless File.exists?("#{$static}/version.cfg")

  # get the versions
  version_b = File.readlines("#{$static}/version.cfg")[0]
  version = version_b.strip[8..-1].to_i
  server_version = open($ini["patch"]["version"]) {|f| f.read.strip[8..-1].to_i}

  if version == server_version
    puts "Patch up to date: v#{server_version}"
  else
    tmp = Dir.mktmpdir
    ((version+1)..server_version).each do |v|
      download = $ini["patch"]["download"] % v
      puts "Downloading #{download}"
      filename = download.split("/")[-1]
      open("#{tmp}/#{filename}", "wb") do |pak|
        open(download) {|f| pak << f.read}
      end
    end

    sh "pak", "-s", "-o",  "--ini", $pak, "-O", $static, tmp
    File.open("#{$static}/version.cfg") {|cfg| cfg.write(version_b.gsub(version.to_s, server_version.to_s))}

    Rake::Task["dnt"].reenable
    Rake::Task["dnt"].invoke

    Rake::Task["images"].reenable
    Rake::Task["images"].invoke
  end
end

task :dnt => ["dnt:process", "dnt:collect"]
namespace :dnt do
  task :process do
    sh "processor", $dnt
  end

  task :collect do
    sh "collector", $dnt
  end
end

task :images do
  dirs = [
    "#{$static}/resource/ui/mainbar",
    "#{$static}/resource/ui/skill"
    ]
    
  dirs.each do |dir|
    Dir.chdir(dir) do
      sh "mogrify -format png *.dds"
      Dir["*.png"].each {|png| sh "pngcrush -ow #{png}"}
    end
  end
end