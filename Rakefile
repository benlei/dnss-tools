require "fileutils"
require "inifile"
require "open-uri"
require "tmpdir"

@ini_path = "#{ENV['HOME']}/.dn"

#fail "You need a #{@ini_path}/patch.ini with proper configurations set."  unless File.exists?("#{@ini_path}/patch.ini")
#fail "You need a #{@ini_path}/pak.ini with proper configurations set." unless File.exists?("#{@ini_path}/pak.ini")
#fail "You need a #{@ini_path}/dnt.ini with proper configurations set." unless File.exists?("#{@ini_path}/dnt.ini")

cwd = Dir.pwd

puts "Loading #{@ini_path}/patch.ini"
@ini = IniFile.load("#{@ini_path}/patch.ini")
@collectable = True # set to false in the update task if update had no changes

desc("Does a full update of the pak files and updates the JSON and pushing to git.")
task :default => [:update, :collect]

desc("Attempts to update the pak to the latest version")
task :update do
  static = @ini["resource"]["location"]
  fail "Cannot find static directory: #{static}" unless File.exists?(static)
  fail "Cannot find version.cfg file: #{static}/version.cfg" unless File.exists?("#{static}/version.cfg")

  # get the version
  version = File.readlines("#{static}/version.config")[0].strip[8..-1].to_i

  fd = open(@ini["patch"]["version"])
  server_version = fd.read.strip[8..-1].to_i
  fd.close

  if version == server_version
    puts "Patch up to date: v#{server_version}"
    @collectable = False
  else
    tmp = Dir.mktmpdir
    ((version+1)..server_version).each do |v|
      download = @ini["patch"]["download"] % v
      filename = download.split("/")[-1]
      open("#{tmp}/#{filename}", "wb") do |file|
        url = open(download)
        file << url.read
        url.close
      end
    end
  end

end


task :collect => "collect:all"
namespace :collect do

  # Just the skillicons
  task :icons do
    Dir.chdir(pwd)
    images_path = Dir.pwd + "/src/main/webapp/resources/icons"
    puts "Clearing #{images_path}..."
    if not File.directory?(images_path)
      FileUtils.mkpath images_path
    elsif
      Dir[images_path + "/*.png"].each do |png|
        FileUtils.rm(png)
      end
    end

    Dir.chdir(resource + "/resource/ui/mainbar")
    puts "Converting .dss files to .png..."
    sh "mogrify -verbose -format png skillicon*.dds"
    puts
    Dir["*.png"].each do |png|
      FileUtils.mv(png, images_path + "/" + png)
    end

    Dir.chdir(images_path)
    puts "compressing png files..."
    Dir["*.png"].each do |png|
      sh "pngcrush -ow #{png}"
    end
  end

  task :all => [:icons, :beans, :json]
end