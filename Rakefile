require 'fileutils'

fail "You need a /home/dnss/.ini/rake.ini with proper configurations set."  unless File.exists?("/home/dnss/.ini/rake.ini")
fail "You need a /home/dnss/.ini/pak.ini with proper configurations set." unless File.exists?("/home/dnss/.ini/pak.ini")
fail "You need a /home/dnss/.ini/dnt.ini with proper configurations set." unless File.exists?("/home/dnss/.ini/dnt.ini")

pwd = Dir.pwd
desc("Attempts to update the pak to the latest version")
task :update do
  
end


#task :collect => "collect:all"
#namespace :collect do
#
#  # Just the skillicons
#  task :icons do
#    Dir.chdir(pwd)
#    images_path = Dir.pwd + "/src/main/webapp/resources/icons"
#    puts "Clearing #{images_path}..."
#    if not File.directory?(images_path)
#      FileUtils.mkpath images_path
#    elsif
#      Dir[images_path + "/*.png"].each do |png|
#        FileUtils.rm(png)
#      end
#    end
#
#    Dir.chdir(resource + "/resource/ui/mainbar")
#    puts "Converting .dss files to .png..."
#    sh "mogrify -verbose -format png skillicon*.dds"
#    puts
#    Dir["*.png"].each do |png|
#      FileUtils.mv(png, images_path + "/" + png)
#    end
#
#    Dir.chdir(images_path)
#    puts "compressing png files..."
#    Dir["*.png"].each do |png|
#      sh "pngcrush -ow #{png}"
#    end
#  end
#
#  task :all => [:icons, :beans, :json]
#end